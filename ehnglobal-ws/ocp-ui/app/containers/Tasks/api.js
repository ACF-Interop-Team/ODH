import {
  BASE_ACTIVITY_DEFINITIONS_API_URL,
  BASE_TASKS_API_URL,
  getEndpoint,
} from 'utils/endpointService';
import queryString from 'utils/queryString';
import request from 'utils/request';
import { requestWithoutJWT } from '../../utils/request';

function getTasks(practitioner, patient, statusList, definition) {
  const baseEndpoint = getEndpoint(BASE_TASKS_API_URL);
  const params = queryString({
    // practitioner,
    patient,
    statusList,
    activityDefinition: definition,
  });
  const requestURL = `${baseEndpoint}${params}`;
  return request(requestURL);
}

function cancelTask(id) {
  const baseEndpoint = getEndpoint(BASE_TASKS_API_URL);
  const requestURL = `${baseEndpoint}/${id}/deactivate`;
  return request(requestURL, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

function getActivityDefinitionsForPractitioner(practitionerId) {
  console.log('getActivityDefinitionsForPractitioner');
  const baseEndpoint = getEndpoint(BASE_ACTIVITY_DEFINITIONS_API_URL);
  const requestURL = `${baseEndpoint}/?practitioner=${practitionerId}`;
  return request(requestURL);
}

function getInitialInterviewToken(surveyId, firstName, lastName, email) {
  const requestURL = `https://us-central1-zanenet-njinck.cloudfunctions.net/DEVELOP-SurveyGenericTokenCreator`; //TODO: update endpoint to cloud function endpoint
  const payload = {
    survey_id: surveyId,
    firstname: firstName,
    lastname: lastName,
    email: email,
  };
  return requestWithoutJWT(requestURL, {
    method: 'POST',
    body: JSON.stringify(payload),
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

function updateTask(taskData) {
  const taskId = taskData.logicalId;
  const baseEndpoint = getEndpoint(BASE_TASKS_API_URL);
  const requestURL = `${baseEndpoint}/${taskId}`;
  return request(requestURL, {
    method: 'PUT',
    body: JSON.stringify(taskData),
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

export const API = {
  getTasks,
  cancelTask,
  getActivityDefinitionsForPractitioner,
  getInitialInterviewToken,
  updateTask,
};
