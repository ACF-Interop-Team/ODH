package gov.samhsa.c2s.c2ssofapi.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import gov.samhsa.c2s.c2ssofapi.config.ConfigProperties;
import gov.samhsa.c2s.c2ssofapi.service.dto.NameDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PageDto;
import gov.samhsa.c2s.c2ssofapi.service.dto.PractitionerDto;
import gov.samhsa.c2s.c2ssofapi.service.exception.ResourceNotFoundException;
import gov.samhsa.c2s.c2ssofapi.service.util.FhirOperationUtil;
import gov.samhsa.c2s.c2ssofapi.service.util.PaginationUtil;
import gov.samhsa.c2s.c2ssofapi.service.util.RichStringClientParam;
import gov.samhsa.c2s.c2ssofapi.web.PractitionerController;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.ResourceType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ca.uhn.fhir.rest.api.Constants.PARAM_LASTUPDATED;
import static java.util.stream.Collectors.toList;


@Service
@Slf4j
public class PractitionerServiceImpl implements PractitionerService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private IGenericClient fhirClient;

    @Autowired
    private ConfigProperties configProperties;

    @Override
    public PractitionerDto getPractitioner(String practitionerId) {
        System.out.println("getPractitioner");
        Bundle practitionerBundle = fhirClient.search().forResource(Practitioner.class)
                .where(new TokenClientParam("_id").exactly().code(practitionerId))
                .revInclude(PractitionerRole.INCLUDE_PRACTITIONER)
                .sort().descending(PARAM_LASTUPDATED)
                .returnBundle(Bundle.class)
                .execute();

        if (practitionerBundle == null || practitionerBundle.getEntry().size() < 1) {
            throw new ResourceNotFoundException("No practitioner was found for the givecn practitionerID:" + practitionerId);
        }

        Bundle.BundleEntryComponent retrievedPractitioner = practitionerBundle.getEntry().get(0);

        PractitionerDto practitionerDto = modelMapper.map(retrievedPractitioner.getResource(), PractitionerDto.class);
        practitionerDto.setLogicalId(retrievedPractitioner.getResource().getIdElement().getIdPart());

        return practitionerDto;
    }

    @Override
    public PageDto<PractitionerDto> searchPractitioners(Optional<PractitionerController.SearchType> type, Optional<String> value, Optional<String> organization, Optional<Boolean> showInactive, Optional<Integer> page, Optional<Integer> size, Optional<Boolean> showAll) {
        System.out.println("searchPractitioners");
        int numberOfPractitionersPerPage = PaginationUtil.getValidPageSize(configProperties, size, ResourceType.Practitioner.name());
        IQuery practitionerIQuery = fhirClient.search().forResource(Practitioner.class).sort().descending(PARAM_LASTUPDATED);


        boolean firstPage = true;
        type.ifPresent(t -> {
                    if (t.equals(PractitionerController.SearchType.name))
                        value.ifPresent(v -> practitionerIQuery.where(new RichStringClientParam("name").contains().value(v.trim())));

                    if (t.equals(PractitionerController.SearchType.identifier))
                        value.ifPresent(v -> practitionerIQuery.where(new TokenClientParam("identifier").exactly().code(v.trim())));
                }
        );

        if (showInactive.isPresent()) {
            if (!showInactive.get())
                practitionerIQuery.where(new TokenClientParam("active").exactly().code("true"));
        } else {
            practitionerIQuery.where(new TokenClientParam("active").exactly().code("true"));
        }

        Bundle firstPagePractitionerSearchBundle;
        Bundle otherPagePractitionerSearchBundle;

        firstPagePractitionerSearchBundle = (Bundle) practitionerIQuery.count(numberOfPractitionersPerPage)
                .revInclude(PractitionerRole.INCLUDE_PRACTITIONER)
                .returnBundle(Bundle.class)
                .execute();

        if (showAll.isPresent() && showAll.get()) {
            List<PractitionerDto> patientDtos = convertAllBundleToSinglePractitionerDtoList(firstPagePractitionerSearchBundle, numberOfPractitionersPerPage);
            return (PageDto<PractitionerDto>) PaginationUtil.applyPaginationForCustomArrayList(patientDtos, patientDtos.size(), Optional.of(1), false);
        }

        if (firstPagePractitionerSearchBundle == null || firstPagePractitionerSearchBundle.isEmpty() || firstPagePractitionerSearchBundle.getEntry().size() < 1) {
            return new PageDto<>(new ArrayList<>(), numberOfPractitionersPerPage, 0, 0, 0, 0);
        }

        otherPagePractitionerSearchBundle = firstPagePractitionerSearchBundle;

        if (page.isPresent() && page.get() > 1 && otherPagePractitionerSearchBundle.getLink(Bundle.LINK_NEXT) != null) {
            firstPage = false;
            otherPagePractitionerSearchBundle = PaginationUtil.getSearchBundleAfterFirstPage(fhirClient, configProperties, firstPagePractitionerSearchBundle, page.get(), numberOfPractitionersPerPage);
        }

        List<Bundle.BundleEntryComponent> retrievedPractitioners = otherPagePractitionerSearchBundle.getEntry();

        return practitionersInPage(retrievedPractitioners, otherPagePractitionerSearchBundle, numberOfPractitionersPerPage, firstPage, page);
    }

    @Override
    public PageDto<PractitionerDto> getPractitioners(Optional<Integer> pageNumber, Optional<Integer> pageSize) {
        System.out.println("getPractitioners");
        int numberOfPractitionersPerPage = PaginationUtil.getValidPageSize(configProperties, pageSize, ResourceType.Practitioner.name());
        boolean firstPage = true;

        IQuery practitionerIQuery = fhirClient.search().forResource(Practitioner.class);

        //Set Sort order
        practitionerIQuery = FhirOperationUtil.setLastUpdatedTimeSortOrder(practitionerIQuery, true);

        Bundle firstPagePractitionerBundle;
        Bundle otherPagePractitionerBundle;

        firstPagePractitionerBundle = (Bundle) practitionerIQuery
                .count(numberOfPractitionersPerPage)
                .revInclude(PractitionerRole.INCLUDE_PRACTITIONER)
                .returnBundle(Bundle.class)
                .execute();

        if (firstPagePractitionerBundle == null || firstPagePractitionerBundle.getEntry().size() < 1) {
            log.info("No practitioners were found for the given criteria.");
            return new PageDto<>(new ArrayList<>(), numberOfPractitionersPerPage, 0, 0, 0, 0);
        }

        otherPagePractitionerBundle = firstPagePractitionerBundle;

        if (pageNumber.isPresent() && pageNumber.get() > 1 && otherPagePractitionerBundle.getLink(Bundle.LINK_NEXT) != null) {
            // Load the required page
            firstPage = false;
            otherPagePractitionerBundle = PaginationUtil.getSearchBundleAfterFirstPage(fhirClient, configProperties, firstPagePractitionerBundle, pageNumber.get(), numberOfPractitionersPerPage);
        }

        List<Bundle.BundleEntryComponent> retrievedPractitioners = otherPagePractitionerBundle.getEntry();

        return practitionersInPage(retrievedPractitioners, otherPagePractitionerBundle, numberOfPractitionersPerPage, firstPage, pageNumber);
    }

    private List<PractitionerDto> convertAllBundleToSinglePractitionerDtoList(Bundle firstPageSearchBundle, int numberOfBundlePerPage) {
        List<Bundle.BundleEntryComponent> bundleEntryComponents = FhirOperationUtil.getAllBundleComponentsAsList(firstPageSearchBundle, Optional.of(numberOfBundlePerPage), fhirClient, configProperties);
        return bundleEntryComponents.stream().filter(pr -> pr.getResource().getResourceType().equals(ResourceType.Practitioner))
                .map(prac -> this.covertEntryComponentToPractitioner(prac, bundleEntryComponents)).collect(toList());
    }

    private PractitionerDto covertEntryComponentToPractitioner(Bundle.BundleEntryComponent practitionerComponent, List<Bundle.BundleEntryComponent> practitionerAndPractitionerRoleList) {
        PractitionerDto practitionerDto = modelMapper.map(practitionerComponent.getResource(), PractitionerDto.class);
        Practitioner practitioner = (Practitioner) practitionerComponent.getResource();
        List<NameDto> nameDtos = new ArrayList<>();
        NameDto nameDto = new NameDto();
        nameDto.setFirstName(practitioner.getName().get(0).getGiven().get(0).getValue());
        nameDto.setLastName(practitioner.getName().get(0).getFamily());
        nameDtos.add(nameDto);
        practitionerDto.setLogicalId(practitionerComponent.getResource().getIdElement().getIdPart());
        practitionerDto.setName(nameDtos);
        return practitionerDto;
    }

    private PageDto<PractitionerDto> practitionersInPage(List<Bundle.BundleEntryComponent> retrievedPractitioners, Bundle otherPagePractitionerBundle, int numberOfPractitionersPerPage, boolean firstPage, Optional<Integer> page) {
        List<PractitionerDto> practitionersList = retrievedPractitioners.stream()
                .filter(retrievedPractitionerAndPractitionerRoles -> retrievedPractitionerAndPractitionerRoles.getResource().getResourceType().equals(ResourceType.Practitioner))
                .map(retrievedPractitioner -> covertEntryComponentToPractitioner(retrievedPractitioner, retrievedPractitioners))
                .collect(toList());

        double totalPages = Math.ceil((double) otherPagePractitionerBundle.getTotal() / numberOfPractitionersPerPage);
        int currentPage = firstPage ? 1 : page.get();

        return new PageDto<>(practitionersList, numberOfPractitionersPerPage, totalPages, currentPage, practitionersList.size(),
                otherPagePractitionerBundle.getTotal());
    }
}
