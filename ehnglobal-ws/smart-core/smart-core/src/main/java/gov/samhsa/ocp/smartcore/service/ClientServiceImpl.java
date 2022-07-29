package gov.samhsa.ocp.smartcore.service;

import feign.FeignException;
import gov.samhsa.ocp.smartcore.config.SmartCoreProperties;
import gov.samhsa.ocp.smartcore.infrastructure.OAuth2ClientRestClient;
import gov.samhsa.ocp.smartcore.infrastructure.dto.ClientDto;
import gov.samhsa.ocp.smartcore.infrastructure.dto.ClientMetaDto;
import gov.samhsa.ocp.smartcore.service.dto.ClientDetailDto;
import gov.samhsa.ocp.smartcore.service.dto.ClientType;
import gov.samhsa.ocp.smartcore.service.dto.KeycloakClientDto;
import gov.samhsa.ocp.smartcore.service.dto.keycloakclient.AllClientsDto;
import gov.samhsa.ocp.smartcore.service.dto.keycloakclient.ClientAccessDto;
import gov.samhsa.ocp.smartcore.service.dto.keycloakclient.ClientAttributesDto;
import gov.samhsa.ocp.smartcore.service.dto.keycloakclient.RetrieveClientDto;
import gov.samhsa.ocp.smartcore.service.exception.ResourceNotFoundException;
import gov.samhsa.ocp.smartcore.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private OAuth2ClientRestClient oAuth2ClientRestClient;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private SmartCoreProperties smartCoreProperties;

    @Override
    @Transactional(readOnly = true)
    public List<ClientMetaDto> getAllClientMeta() {
        //        return oAuth2ClientRestClient.getAllClientMeta().stream()
//                .filter(meta -> StringUtils.hasText(meta.getClientId()))
//                .collect(Collectors.toList());

        List<AllClientsDto> allClientsDtos = oAuth2ClientRestClient.getAllClients().stream()
                .filter(clientDetailDto -> clientDetailDto.getClientId() != null && !clientDetailDto.getClientId().isEmpty())
                .filter(clientDetailDto -> clientDetailDto.getAttributes().getSmart_app_condition() != null)
                .filter(clientDetailDto -> clientDetailDto.getAttributes().getSmart_app_condition().equalsIgnoreCase("true")).collect(Collectors.toList());

        List<ClientMetaDto> clientMetaDtos =  new ArrayList<>();

        allClientsDtos.forEach(allClientsDto -> {
            ClientMetaDto clientMetaDto = new ClientMetaDto();
            clientMetaDto.setClientId(allClientsDto.getId());
            clientMetaDto.setClientName(allClientsDto.getClientId());
            // TODO: APP ICON
            clientMetaDto.setAppIcon("https://cdn.pixabay.com/photo/2017/01/29/21/16/nurse-2019420_960_720.jpg");
            clientMetaDtos.add(clientMetaDto);
        });

        System.out.println("getAllClientMeta");
        return clientMetaDtos;
//        return oAuth2ClientRestClient.getAllClientMeta().stream().map(el -> {
//            ClientMetaDto clientMetaDto = new ClientMetaDto();
//            clientMetaDto.setClientId(el.getId());
//            try {
//                if(!el.getName().isEmpty()) {
//                    clientMetaDto.setClientName(el.getName());
//                }
//            } catch (NullPointerException e) {
//                clientMetaDto.setClientName(el.getClientId());
//            }
////            if(!el.getName().isEmpty() && el.getName() != null) {
////                clientMetaDto.setClientName(el.getName());
////            } else {
////                clientMetaDto.setClientName(el.getClientId());
////            }
//            clientMetaDto.setAppIcon("https://cdn.pixabay.com/photo/2017/01/29/21/16/nurse-2019420_960_720.jpg");
//            return clientMetaDto;
//        }).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void createClient(ClientDetailDto clientDetailDto) {
        KeycloakClientDto keycloakClientDto = new KeycloakClientDto();
        ClientAccessDto clientAccessDto = new ClientAccessDto();
        ClientAttributesDto attributesDto = new ClientAttributesDto();

        clientAccessDto.setConfigure(true);
        clientAccessDto.setManage(true);
        clientAccessDto.setView(true);
        keycloakClientDto.setAccess(clientAccessDto);

        if(clientDetailDto.getClientType().equals(ClientType.PUBLIC)) {
            keycloakClientDto.setPublicClient(true);
        }

        keycloakClientDto.setClientId(clientDetailDto.getClientId());
        keycloakClientDto.setName(clientDetailDto.getName());
        keycloakClientDto.setEnabled(true);
        keycloakClientDto.setProtocol("openid-connect");

        // It could be changed from the front-end
        keycloakClientDto.setDescription(clientDetailDto.getName());

        // Attributes
        attributesDto.setSmart_app_condition("true");

        if(clientDetailDto.getAppIcon() != null) {
            System.out.println(clientDetailDto.getAppIcon());
            attributesDto.setSmart_app_icon(clientDetailDto.getAppIcon());
        }

        keycloakClientDto.setAttributes(attributesDto);

        keycloakClientDto.setBaseUrl(clientDetailDto.getAppLaunchUrl());
        keycloakClientDto.setDirectAccessGrantsEnabled(true);
        keycloakClientDto.setStandardFlowEnabled(true);

        String[] redirects = new String[clientDetailDto.getRedirectUris().length];

        System.arraycopy(clientDetailDto.getRedirectUris(), 0, redirects, 0, clientDetailDto.getRedirectUris().length);

        keycloakClientDto.setRedirectUris(redirects);

        String[] clientscopes = new String[clientDetailDto.getDefaultClientScopes().length];

        for(String scope: clientscopes) {
            log.info(scope);
        }

        System.arraycopy(clientDetailDto.getDefaultClientScopes(), 0, clientscopes, 0, clientDetailDto.getDefaultClientScopes().length);
        keycloakClientDto.setDefaultClientScopes(clientscopes);

        try{
            oAuth2ClientRestClient.createClient(keycloakClientDto);
        }catch (FeignException e) {
            System.out.println(e.getMessage());
            ExceptionUtil.handleFeignException(e, "Failed to create client with: " + clientDetailDto.getClientId());
        }

//        if(clientDetailDto.getClientType().equals(ClientType.PUBLIC)){
//            clientDetailDto.setClientSecret(smartCoreProperties.getPublicClientSecret());
//        }
//        try {
//            final ClientDto clientDto = oAuth2ClientRestClient.createClient(modelMapper.map(clientDetailDto, ClientDto.class));
//            final ClientMetaDto clientMetaDto = oAuth2ClientRestClient.createClientMeta(clientDto.getClientId(), modelMapper.map(clientDetailDto, ClientMetaDto.class));
//        } catch (FeignException fe){
//            ExceptionUtil.handleFeignException(fe, "Failed to create client with: " + clientDetailDto.getClientId());
//        }
    }

    @Override
    @Transactional
    public void updateClient(String clientId, ClientDetailDto clientDetailDto) {
            List<RetrieveClientDto> data = oAuth2ClientRestClient.getIDClient(clientId);
            KeycloakClientDto keycloakClientDto = new KeycloakClientDto();
            ClientAccessDto clientAccessDto = new ClientAccessDto();
            ClientAttributesDto attributesDto = new ClientAttributesDto();

            if(data.size() > 0) {
              String client_id = data.get(0).getId();
              try {

                  oAuth2ClientRestClient.deleteClient(client_id);

                  clientAccessDto.setConfigure(true);
                  clientAccessDto.setManage(true);
                  clientAccessDto.setView(true);
                  keycloakClientDto.setAccess(clientAccessDto);

                  if(clientDetailDto.getClientType().equals(ClientType.PUBLIC)) {
                      keycloakClientDto.setPublicClient(true);
                  }

                  keycloakClientDto.setClientId(clientDetailDto.getClientId());
                  keycloakClientDto.setName(clientDetailDto.getName());
                  keycloakClientDto.setEnabled(true);
                  keycloakClientDto.setProtocol("openid-connect");

                  // It could be changed from the front-end
                  keycloakClientDto.setDescription(clientDetailDto.getName());

                  // Attributes
                  attributesDto.setSmart_app_condition("true");

                  if(clientDetailDto.getAppIcon() != null) {
                      System.out.println(clientDetailDto.getAppIcon());
                      attributesDto.setSmart_app_icon(clientDetailDto.getAppIcon());
                  }

                  keycloakClientDto.setAttributes(attributesDto);

                  keycloakClientDto.setBaseUrl(clientDetailDto.getAppLaunchUrl());
                  keycloakClientDto.setDirectAccessGrantsEnabled(true);
                  keycloakClientDto.setStandardFlowEnabled(true);

                  String[] redirects = new String[clientDetailDto.getRedirectUris().length];

                  System.arraycopy(clientDetailDto.getRedirectUris(), 0, redirects, 0, clientDetailDto.getRedirectUris().length);

                  keycloakClientDto.setRedirectUris(redirects);

                  String[] clientscopes = new String[clientDetailDto.getDefaultClientScopes().length];

                  for(String scope: clientscopes) {
                      log.info(scope);
                  }

                  System.arraycopy(clientDetailDto.getDefaultClientScopes(), 0, clientscopes, 0, clientDetailDto.getDefaultClientScopes().length);
                  keycloakClientDto.setDefaultClientScopes(clientscopes);

                  try{
                      oAuth2ClientRestClient.createClient(keycloakClientDto);
                  }catch (FeignException e) {
                      log.error("Failed to create client: " + e.getMessage());
                      ExceptionUtil.handleFeignException(e, "Failed to create client with: " + clientDetailDto.getClientId());
                  }

              } catch (FeignException e) {
                  log.error("Failed to delete client: " + e.getMessage());
                  ExceptionUtil.handleFeignException(e, "Failed to delete client");
              }
          } else {
                throw new ResourceNotFoundException("Client not found");
          }

//        KeycloakClientDto keycloakClientDto = new KeycloakClientDto();
//        ClientAccessDto clientAccessDto = new ClientAccessDto();
//
//        clientAccessDto.setConfigure(true);
//        clientAccessDto.setManage(true);
//        clientAccessDto.setView(true);
//        keycloakClientDto.setAccess(clientAccessDto);
//
//        if(clientDetailDto.getClientType().equals(ClientType.PUBLIC)) {
//            keycloakClientDto.setPublicClient(true);
//        }
//
//        keycloakClientDto.setClientId(clientDetailDto.getClientId());
//        keycloakClientDto.setName(clientDetailDto.getName());
//        keycloakClientDto.setEnabled(true);
//        keycloakClientDto.setProtocol("openid-connect");
//
//        keycloakClientDto.setDescription(clientDetailDto.getName());
//
//        keycloakClientDto.setBaseUrl(clientDetailDto.getAppLaunchUrl());
//        keycloakClientDto.setDirectAccessGrantsEnabled(true);
//        keycloakClientDto.setStandardFlowEnabled(true);
//
//        String[] redirects = new String[clientDetailDto.getRedirectUris().length];
//
//        System.arraycopy(clientDetailDto.getRedirectUris(), 0, redirects, 0, clientDetailDto.getRedirectUris().length);
//
//        keycloakClientDto.setRedirectUris(redirects);
//
//        String[] clientscopes = new String[clientDetailDto.getDefaultClientScopes().length];
//
//        for(String scope: clientscopes) {
//            log.info(scope);
//        }
//
//        System.arraycopy(clientDetailDto.getDefaultClientScopes(), 0, clientscopes, 0, clientDetailDto.getDefaultClientScopes().length);
//        keycloakClientDto.setDefaultClientScopes(clientscopes);
//
//        List<RetrieveClientDto> data = oAuth2ClientRestClient.getIDClient(clientId);
//
//        if(data.size() > 0) {
//            String client_id = data.get(0).getId();
//            try {
//                oAuth2ClientRestClient.updateClient(client_id, keycloakClientDto);
//            }catch (FeignException e) {
//                ExceptionUtil.handleFeignException(e, "Failed to update client with:: " + clientId);
//            }
//        } else {
//            throw new ResourceNotFoundException("Client Not found");
//        }

        //oAuth2ClientRestClient.updateClient(clientId, keycloakClientDto);
//        try {
//            final ClientDto clientDto = oAuth2ClientRestClient.updateClient(clientId, modelMapper.map(clientDetailDto, ClientDto.class));
//            final ClientMetaDto clientMetaDto = oAuth2ClientRestClient.createClientMeta(clientDto.getClientId(), modelMapper.map(clientDetailDto, ClientMetaDto.class));
//        }catch (FeignException fe){
//            ExceptionUtil.handleFeignException(fe, "Failed to update client with:: " + clientId);
//        }
    }

    @Override
    @Transactional
    public void deleteClient(String clientId) {
        List<RetrieveClientDto> data = oAuth2ClientRestClient.getIDClient(clientId);
        if(data.size() > 0) {
            String client_id = data.get(0).getId();
            try {
                oAuth2ClientRestClient.deleteClient(client_id);
            } catch (FeignException e) {
                ExceptionUtil.handleFeignException(e, "Failed to delete client with:: " + clientId);
            }
        } else {
            throw new ResourceNotFoundException("Client Not found");
        }
//        oAuth2ClientRestClient.deleteClient(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDetailDto> getAllClients() {
        List<ClientDetailDto> clientDetailDtos = new ArrayList<>();

        List<AllClientsDto> allClientsDtos = oAuth2ClientRestClient.getAllClients().stream()
                .filter(clientDetailDto -> clientDetailDto.getClientId() != null && !clientDetailDto.getClientId().isEmpty())
                .filter(clientDetailDto -> clientDetailDto.getAttributes().getSmart_app_condition() != null)
                .filter(clientDetailDto -> clientDetailDto.getAttributes().getSmart_app_condition().equalsIgnoreCase("true")).collect(Collectors.toList());

        allClientsDtos.forEach(allClientsDto -> {
            ClientDetailDto clientDetailDto = new ClientDetailDto();

            clientDetailDto.setId(allClientsDto.getId());

            if(allClientsDto.getPublicClient()) {
                clientDetailDto.setClientType(ClientType.PUBLIC);
            } else {
                clientDetailDto.setClientType(ClientType.CONFIDENTIAL);
            }

            clientDetailDto.setClientId(allClientsDto.getClientId());
            clientDetailDto.setRedirectUris(allClientsDto.getRedirectUris());
            clientDetailDto.setDefaultClientScopes(allClientsDto.getDefaultClientScopes());
            clientDetailDto.setName(allClientsDto.getClientId());
            clientDetailDto.setAppLaunchUrl(allClientsDto.getBaseUrl());
            clientDetailDtos.add(clientDetailDto);
        });

        return clientDetailDtos;

//        List<ClientMetaDto> clientMetaDtos = oAuth2ClientRestClient.getAllClientMeta().stream()
//                .filter(meta -> StringUtils.hasText(meta.getClientId()))
//                .collect(Collectors.toList());
//
//        System.out.println("getAllClients");
//        List<ClientDto> clientDtos = oAuth2ClientRestClient.getAllClients();
//
//        List<ClientDetailDto> clientDetailDtos = new ArrayList<>();
//
//        clientMetaDtos
//                .forEach(clientMetaDto -> {
//                        ClientDto client = clientDtos.stream().filter(clientDto -> clientDto.getClientId().equals(clientMetaDto.getClientId())).findFirst().get();
//                        ClientDetailDto clientDetailDto = modelMapper.map(client, ClientDetailDto.class);
//                        //clientDetailDto.setAppIcon("https://cdn.pixabay.com/photo/2017/01/29/21/16/nurse-2019420_960_720.jpg");
//                        clientDetailDto.setClientId(clientMetaDto.getId());
//                        clientDetailDto.setName(clientMetaDto.getClientId());
//                        //clientDetailDto.setAppLaunchUrl(clientMetaDto.getAppLaunchUrl());
//                        clientDetailDtos.add(clientDetailDto);
//                });

//        return clientDetailDtos;

    }
}
