# Nexus3 Gitlab Auth Plugin
This plugin adds a Gitlab realm to Sonatype Nexus OSS and enables you to authenticate with Gitlab Users and authorize with Gitlab Groups.

The plugin does not implement a full OAuth flow, instead you use your gitlab user name + an gitlab read_user token you generated in your account to log in to the nexus.
This works through the web as well as through tools like maven, gradle etc.

## Setup

#### 1. Activate the Realm
Log in to your nexus and go to _Administration > Security > Realms_. Move the Gitlab Realm to the right. The realm order in the form determines the order of the realms in your authentication flow. We recommend putting Gitlab _after_ the built-in realms:

#### 2. Group / Roles Mapping
When logged in through Gitlab, all groups the user is a member of will be mapped into roles :


You need to manually create these roles in _Administration > Security > Roles > (+) Create Role > Nexus Role_ in order to assign them the desired privileges. The _Role ID_ should map to the _group name_. Note that by default nobody is allowed to login (authenticate).

## Usage

The following steps need to be done by every developer who wants to login to your nexus with Gitlab.
#### 1. Generate API Token

In your Gitlab account under generate a new token with read_user privilege. 

#### 2. Login to nexus

When logging in to nexus, use your gitlab user name as the username and the token you just generated as the password.
This also works through maven, gradle etc.

## Installation

#### 0. Prerequisites

##### Directory naming convention:
For the following commands we assume your nexus installation resides in `/opt/sonatype/nexus`. See [https://books.sonatype.com/nexus-book/reference3/install.html#directories](https://books.sonatype.com/nexus-book/reference3/install.html#directories) for reference.

#### 1. Download and install

The following lines will:
- create a directory in the `nexus` / `kafka` maven repository
- download the latest release from gitlab
- unzip the release to the maven repository
- add the plugin to the `karaf` `startup.properties`.
```shell
mkdir -p /opt/sonatype/nexus/system/com/github/oassuncao/ &&\
wget -O /opt/sonatype/nexus/system/com/github/oassuncao/nexus-gitlab-plugin-1.1.0.jar https://github.com/oassuncao/nexus-gitlab-plugin/releases/download/v1.0.0/nexus-gitlab-plugin-1.1.0.jar &&\
echo "reference\:file\:com/github/oassuncao/nexus-gitlab-plugin-1.1.0.jar = 200" >> /opt/sonatype/nexus/etc/karaf/startup.properties
```

#### 2. Create configuration
The configuration is done using Capabilities `Gitlab Realm`

#### 3. Restart Nexus
Restart your Nexus instance to let it pick up your changes.

## Development
You can build the project with the integrated maven wrapper like so: `./mvnw clean package`

You can also build locally using Docker by running `docker run --rm -it -v $(pwd):/data -w /data maven:3.5.2 mvn clean package`

You can build a ready to run docker image using the [`Dockerfile`](Dockerfile) to quickly spin up a nexus with the plugin already preinstalled.

## Credits

The whole project is heavily influenced by the, [nexus3-github-oauth-plugin](https://github.com/larscheid-schmitzhermes/nexus3-github-oauth-plugin) itself influenced by the [nexus3-crowd-plugin](https://github.com/pingunaut/nexus3-crowd-plugin).
