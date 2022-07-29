package gov.samhsa.ocp.ocpfis.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.validation.FhirValidator;
import gov.samhsa.ocp.ocpfis.config.FisProperties;
import gov.samhsa.ocp.ocpfis.service.dto.*;
import gov.samhsa.ocp.ocpfis.service.dto.valueset.Coding;
import gov.samhsa.ocp.ocpfis.service.exception.BinaryNotFoundException;
import gov.samhsa.ocp.ocpfis.service.exception.DocumentReferenceNotFoundException;
import gov.samhsa.ocp.ocpfis.util.*;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseHasExtensions;
import org.hl7.fhir.r4.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class DocumentReferenceServiceImpl implements DocumentReferenceService{
    private final ModelMapper modelMapper;
    private final IGenericClient fhirClient;
    private final FisProperties fisProperties;
    private final FhirValidator fhirValidator;
    private final LookUpService lookUpService;
    private final ProvenanceUtil provenanceUtil;

    public DocumentReferenceServiceImpl(ModelMapper modelMapper, IGenericClient fhirClient, FisProperties fisProperties, FhirValidator fhirValidator, LookUpService lookUpService, ProvenanceUtil provenanceUtil){
        this.modelMapper = modelMapper;
        this.fhirClient = fhirClient;
        this.fisProperties = fisProperties;
        this.fhirValidator = fhirValidator;
        this.lookUpService = lookUpService;
        this.provenanceUtil = provenanceUtil;
    }

    @Override
    public DocumentReferenceDto getDocumentById(String id) {
        final DocumentReference retrievedocument = fhirClient.read().resource(DocumentReference.class).withId(id).execute();
        if(retrievedocument == null || retrievedocument.isEmpty()){
            throw new DocumentReferenceNotFoundException("No DocumentReference Found");
        }
        //final DocumentReferenceDto documentReferenceDto = modelMapper.map(retrievedocument, DocumentReferenceDto.class);
        final DocumentReferenceDto documentReferenceDto = mapperDocumentReference(retrievedocument);
        documentReferenceDto.setLogicalId(retrievedocument.getIdElement().getIdPart());
        return documentReferenceDto;
    }

    @Override
    public PageDto<DocumentReferenceDto> getAllDocuments(Optional<Boolean> showInactive, Optional<Integer> page, Optional<Integer> size) {
        int numberofDocumentsPerPage = PaginationUtil.getValidPageSize(fisProperties, size, ResourceType.DocumentReference.name());
        IQuery documentreferenceIQuery = fhirClient.search().forResource(DocumentReference.class);
        documentreferenceIQuery = FhirOperationUtil.setLastUpdatedTimeSortOrder(documentreferenceIQuery, true);
        Bundle firstPageDocumentSearchBundle;
        Bundle otherPageDocumentSearchBundle;
        boolean firstPage = true;

        firstPageDocumentSearchBundle = PaginationUtil.getSearchBundleFirstPage(documentreferenceIQuery, numberofDocumentsPerPage, Optional.empty());

        if(firstPageDocumentSearchBundle == null || firstPageDocumentSearchBundle.getEntry().size() < 1){
            log.info("No Document References were found for the given criteria");
            return new PageDto<>(new ArrayList<>(), numberofDocumentsPerPage, 0,0,0,0);
        }

        otherPageDocumentSearchBundle = firstPageDocumentSearchBundle.copy();

        if(page.isPresent() && page.get() > 1 && otherPageDocumentSearchBundle.getLink(Bundle.LINK_NEXT) != null){
            firstPage = false;
            otherPageDocumentSearchBundle = PaginationUtil.getSearchBundleAfterFirstPage(fhirClient, fisProperties, firstPageDocumentSearchBundle, page.get(), numberofDocumentsPerPage);
        }

        List<Bundle.BundleEntryComponent> retrieveDocuments = otherPageDocumentSearchBundle.getEntry();

        /*List<DocumentReferenceDto> documentList = retrieveDocuments.stream().map(retrieveDocument -> {
            DocumentReferenceDto documentReferenceDto =  modelMapper.map(retrieveDocument.getResource(), DocumentReferenceDto.class);
            documentReferenceDto.setLogicalId(retrieveDocument.getResource().getIdElement().getIdPart());
            return documentReferenceDto;
        }).collect(toList());*/
        List<DocumentReferenceDto> documentList = convertToDocumentReference(retrieveDocuments);

        double totalPages = Math.ceil((double) otherPageDocumentSearchBundle.getTotal() / numberofDocumentsPerPage);
        int currentPage = firstPage ? 1 : page.get();

        return new PageDto<>(documentList, numberofDocumentsPerPage, totalPages, currentPage, documentList.size(), otherPageDocumentSearchBundle.getTotal());
    }

    @Override
    public PageDto<DocumentReferenceDto> getDocumentbyPatientId(String patientId, Optional<String> searchKey, Optional<String> searchValue, Optional<Boolean> showInactive, Optional<Integer> pageNumber, Optional<Integer> pageSize, Optional<Boolean> showAll) {
        int numberPerPage = PaginationUtil.getValidPageSize(fisProperties, pageSize, ResourceType.DocumentReference.name());
        IQuery documentreferenceQuery = fhirClient.search().forResource(DocumentReference.class).where(new ReferenceClientParam("patient").hasId("Patient/"+patientId));

        documentreferenceQuery = FhirOperationUtil.setLastUpdatedTimeSortOrder(documentreferenceQuery, true);

        Bundle firstPageBundle;
        Bundle otherPageBundle;
        boolean firstPage = true;

//        System.out.println(patientId);
//        System.out.println(documentreferenceQuery.toString());

        firstPageBundle = (Bundle) documentreferenceQuery.count(numberPerPage).returnBundle(Bundle.class).execute();

        if(firstPageBundle == null || firstPageBundle.getEntry().isEmpty()){
            log.info("No DocumentReference was found for this given criteria");
            return new PageDto<>(new ArrayList<>(), numberPerPage, 0,0,0,0);
        }

        otherPageBundle = firstPageBundle.copy();

        if(pageNumber.isPresent() && pageNumber.get() > 1 && otherPageBundle.getLink(Bundle.LINK_NEXT) != null){
            firstPage = false;
            otherPageBundle = PaginationUtil.getSearchBundleAfterFirstPage(fhirClient, fisProperties, firstPageBundle, pageNumber.get(), numberPerPage);
        }

        List<Bundle.BundleEntryComponent> retrieveDocuments = otherPageBundle.getEntry();

        /*List<DocumentReferenceDto> documentList = retrieveDocuments.stream().map(retrieveDocument -> {
            DocumentReferenceDto documentReferenceDto = modelMapper.map(retrieveDocument.getResource(), DocumentReferenceDto.class);
            DocumentReferenceDto documentReferenceDto  = mapperDocumentReference(retrieveDocument.getResource());
            documentReferenceDto.setLogicalId(retrieveDocument.getResource().getIdElement().getIdPart());
            return documentReferenceDto;
        }).collect(toList());*/
        List<DocumentReferenceDto> documentList = convertToDocumentReference(retrieveDocuments);

        double totalPages = Math.ceil((double) otherPageBundle.getTotal() / numberPerPage);
        int currentPage = firstPage ? 1 : pageNumber.get();


        return new PageDto<>(documentList, numberPerPage, totalPages, currentPage, documentList.size(), otherPageBundle.getTotal());
    }

    private List<DocumentReferenceDto> convertToDocumentReference(List<Bundle.BundleEntryComponent> retrieveDocuments){
        return retrieveDocuments.stream().map(this::mapperDocumentReference).collect(toList());
    }

    private DocumentReferenceDto mapperDocumentReference(Object documentReference){
        DocumentReferenceDto documentReferenceDto = new DocumentReferenceDto();
        DocumentReference docReference;
        if(documentReference instanceof  Bundle.BundleEntryComponent){
            docReference = (DocumentReference) ((Bundle.BundleEntryComponent) documentReference).getResource();
        } else if(documentReference instanceof DocumentReference){
            docReference = (DocumentReference) documentReference;
        } else return null;

        documentReferenceDto.setLogicalId(docReference.getIdElement().getIdPart());
        documentReferenceDto.setStatus(docReference.getStatus().getDefinition());

        CodeDto type = new CodeDto();
        type.setCoding(docReference.getType().getCoding().stream().map(coding -> {
            Coding res = new Coding();
            res.setCode(coding.getCode());
            res.setDisplay(coding.getDisplay());
            res.setSystem(coding.getSystem());
            return res;
        }).collect(toList()));
        documentReferenceDto.setType(type);

        documentReferenceDto.setSubject(new SubjectReferenceDto(docReference.getSubject().getReference()));

        if(!docReference.isEmpty() && docReference.getDate() != null){
            documentReferenceDto.setDate(docReference.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate());
        }

        documentReferenceDto.setAuthor(docReference.getAuthor().stream().map(author -> {
            AuthorDto res = new AuthorDto();
            res.setReference(author.getReference());
            res.setType(author.getType());
            return res;
        }).collect(toList()));

        documentReferenceDto.setContent(docReference.getContent().stream().map( doc -> {
            AttachmentDto attach = new AttachmentDto();
            ContentAttachmentDto content = new ContentAttachmentDto();
            content.setContentType(doc.getAttachment().getContentType());
            content.setCreation(doc.getAttachment().getCreation().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate());
            content.setTitle(doc.getAttachment().getTitle());
            content.setUrl(doc.getAttachment().getUrl());
            attach.setAttachment(content);
            return attach;
        }).collect(toList()));
        return documentReferenceDto;
    }

    @Override
    public void createDocumentReference(DocumentReferenceDto documentReferenceDto) {
        List<String> idList = new ArrayList<>();
        DocumentReference fhirDocumentReference = modelMapper.map(documentReferenceDto, DocumentReference.class);

        FhirOperationUtil.validateFhirResource(fhirValidator, fhirDocumentReference, Optional.empty(), ResourceType.DocumentReference.name(), "Create DocumentReference");
        MethodOutcome serverResponse = FhirOperationUtil.createFhirResource(fhirClient, fhirDocumentReference, ResourceType.DocumentReference.name());
        idList.add(ResourceType.DocumentReference.name() + "/" + FhirOperationUtil.getFhirId(serverResponse));

        ActivityDefinition activityDefinition = FhirResourceUtil.createToDoActivityDefinition(serverResponse.getId().getIdPart(), fisProperties, lookUpService, fhirClient);
        FhirProfileUtil.setActivityDefinitionProfileMetaData(fhirClient, activityDefinition);

        FhirOperationUtil.validateFhirResource(fhirValidator, activityDefinition, Optional.empty(), ResourceType.ActivityDefinition.name(),"Create ActivityDefinition (when creating an DocumentReference)" );

        MethodOutcome activityDefinitionOutcome = FhirOperationUtil.createFhirResource(fhirClient, activityDefinition, ResourceType.AuditEvent.name());
        idList.add(ResourceType.ActivityDefinition.name() + "/" + FhirOperationUtil.getFhirId(activityDefinitionOutcome));

//        if(fisProperties.isProvenanceEnabled()){
//            provenanceUtil.createProvenance(idList, ProvenanceActivityEnum.CREATE, loggedInUser);
//        }
    }


    @Override
    public BinaryDto getBinaryById(String id) {
        final Binary retrieveBinary = fhirClient.read().resource(Binary.class).withId(id).execute();
        if(retrieveBinary == null || retrieveBinary.isEmpty()){
            throw new BinaryNotFoundException("Binary not found");
        }

        final BinaryDto binaryDto = modelMapper.map(retrieveBinary, BinaryDto.class);
        binaryDto.setLogicalId(retrieveBinary.getIdElement().getIdPart());

        return binaryDto;
    }
}
