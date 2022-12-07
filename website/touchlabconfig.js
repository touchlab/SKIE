const orgName = "touchlab";
const repoName = "SitePlayground";

const docusaurusConfig = {
    title: "SKIE",
    tagline: "Swift-friendly API generator for Kotlin Multiplatform Mobile",
    url: `https://green-smoke-0862bf810.2.azurestaticapps.net/`,
    baseUrl: `/`,
    organizationName: orgName, // Usually your GitHub org/user name.
    projectName: repoName, // Usually your repo name.

};

const isDev = process.env.NODE_ENV === 'development'

const extraConfig = {
    trackingID: isDev ? 'G-2WF0ECB9L3' : 'G-FBF8S0GPSW',
}

module.exports = {
    docusaurusConfig: docusaurusConfig,
    extraConfig: extraConfig
};
