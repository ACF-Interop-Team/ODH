import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import { compose } from 'redux';
import { createStructuredSelector } from 'reselect';
import TableauReport from 'tableau-react';
import { env_vars } from '../../../env';
import {
  BASE_TICKET_DASHBOARD,
  getEndpoint,
} from '../../utils/endpointService';
import request from '../../utils/request';
import {
  CARE_COORDINATOR_ROLE_CODE,
  CARE_MANAGER_ROLE_CODE,
  ORGANIZATION_ADMIN_ROLE_CODE,
  PCP_ROLE_CODE,
} from '../App/constants';
import { makeSelectUser } from '../App/contextSelectors';
import has from 'lodash/has';

const options = {
  height: 856,
  width: '100%',
  hideTabs: false,
  // All other vizCreate options are supported here, too
  // They are listed here: https://help.tableau.com/current/api/js_api/en-us/JavaScriptAPI/js_api_ref.htm#vizcreateoptions_record
};

const filters = {
  Colors: ['Blue', 'Red'],
  Sizes: ['Small', 'Medium'],
};

const orgAdminDashboard = {
  host_url: env_vars.REACT_APP_ORG_ADMIN_DASHBOARD_HOST_URL,
  name: env_vars.REACT_APP_ORG_ADMIN_DASHBOARD_VIEW_NAME,
  showAppBanner: 'false',
  display_count: 'no',
  showVizHome: 'no',
};

const careManagerDashboard = {
  host_url: env_vars.REACT_APP_CARE_MANAGER_DASHBOARD_HOST_URL,
  name: env_vars.REACT_APP_CARE_MANAGER_DASHBOARD_VIEW_NAME,
  showAppBanner: 'false',
  display_count: 'no',
  showVizHome: 'no',
};

const careCoordinatorDashboard = {
  host_url: env_vars.REACT_APP_CARE_COORDINATOR_DASHBOARD_HOST_URL,
  name: env_vars.REACT_APP_CARE_COORDINATOR_DASHBOARD_VIEW_NAME,
  showAppBanner: 'false',
  display_count: 'no',
  showVizHome: 'no',
};

const defaultParameters = {
  embed_code_version: '3',
  site_root: '',
  tabs: 'yes',
  toolbar: 'yes',
  animate_transition: 'yes',
  display_static_image: 'yes',
  display_spinner: 'yes',
  display_overlay: 'yes',
  display_count: 'yes',
  language: 'es-ES',
};

// eslint-disable-next-line react/prefer-stateless-function
export class Dashboardtableu extends React.Component {
  // eslint-disable-next-line no-useless-constructor
  constructor(props) {
    super(props);
    this.state = {
      query: null,
      ticket: null,
      error: false,
      erroruser: false,
      notallowed: false,
      loading: true,
    };

    this.getParamsByUserRole = this.getParamsByUserRole.bind(this);
  }

  componentDidMount() {
    console.log('Dashboard Mounted');
    console.log('Dashboard state:');
    console.log(this.state);
    this.getPractitionerId();
    this.getTicket();
  }

  componentDidUpdate() {
    console.log('Dashboard Updated');
    console.log('Dashboard state:');
    console.log(this.state);
    // this.getPractitionerId();
    // this.getTicket();
  }

  getTicket() {
    const { user } = this.props;
    const baseEndpoint = getEndpoint(BASE_TICKET_DASHBOARD);
    const url = `${baseEndpoint}?email=${user.email}`;
    request(url)
      .then((data) => {
        console.log(`Response Ticket: ${data}`);
        // eslint-disable-next-line no-unused-expressions
        data.ticket === '-1' || data.ticket === '-1\n'
          ? this.setState({ notallowed: true, loading: false })
          : this.setState({ ticket: data.ticket, loading: false });
      })
      .catch(() => {
        console.log('Could not retrieve Ticket');
        this.setState({ error: true, loading: false });
      });
  }

  getPractitionerId() {
    const { user } = this.props;
    let practitionerId = null;
    if (user) {
      if (
        user.role === CARE_COORDINATOR_ROLE_CODE ||
        user.role === CARE_MANAGER_ROLE_CODE ||
        user.role === PCP_ROLE_CODE ||
        user.role === ORGANIZATION_ADMIN_ROLE_CODE
      ) {
        if (has(user, 'fhirResource')) {
          if (has(user, 'fhirResource.logicalId')) {
            practitionerId = user.fhirResource.logicalId;
          }
        }
      }
    }
    // const practitionerId =
    //   user &&
    //   (user.role === CARE_COORDINATOR_ROLE_CODE ||
    //     user.role === CARE_MANAGER_ROLE_CODE ||
    //     user.role === PCP_ROLE_CODE ||
    //     user.role === ORGANIZATION_ADMIN_ROLE_CODE)
    //     ? user.user_id
    //     : null;

    // console.log('DashboardTableu user ', user);
    console.log('DashboardTableu practitionerId ', practitionerId);

    if (practitionerId) {
      this.setState({
        query: `?:embed=yes&providerUUID=${practitionerId}`,
      });
    } else {
      this.setState({ erroruser: true });
    }
  }

  getParamsByUserRole() {
    const { user } = this.props;

    switch (user.role) {
      case CARE_COORDINATOR_ROLE_CODE:
        return { ...defaultParameters, ...careCoordinatorDashboard };

      case CARE_MANAGER_ROLE_CODE:
        return { ...defaultParameters, ...careManagerDashboard };

      case ORGANIZATION_ADMIN_ROLE_CODE:
        return { ...defaultParameters, ...orgAdminDashboard };

      default:
        return { ...defaultParameters, ...careCoordinatorDashboard };
    }
  }

  render() {
    const params = this.getParamsByUserRole();
    const tempReportUrl = params.host_url + 'views/' + params.name;
    const reportUrl = tempReportUrl.replaceAll('&#47;', '/');

    console.log('DashboardTableu params', params);
    console.log('DashboardTableu reportUrl', reportUrl);

    return (
      <div>
        {/* {this.state.query && this.state.ticket ? ( */}
        {this.state.query ? (
          <TableauReport
            url={reportUrl}
            filters={filters}
            parameters={params}
            options={options} // vizCreate options
            query={this.state.query}
            token={this.state.ticket}
          />
        ) : (
          <div>
            {this.state.error && (
              <h1>There is an error loading the Dashboard</h1>
            )}
            {this.state.erroruser && (
              <h1>Error retrieving the practitioner ID</h1>
            )}
            {this.state.notallowed && (
              <h1>Please ask for permissions to view this dashboard</h1>
            )}
            {this.state.loading && <h1>Loading Dashboard...</h1>}
          </div>
        )}
      </div>
    );
  }
}

Dashboardtableu.propTypes = {
  user: PropTypes.object,
};

const mapStateToProps = createStructuredSelector({
  user: makeSelectUser(),
});

const withConnect = connect(mapStateToProps);

export default compose(withConnect)(Dashboardtableu);
