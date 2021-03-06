# Newsifier

Application that uses IBM Watson services to create, train and test a NLC Classifier of news

## Bluemix Services used

* [Cloudant NoSQL DB](https://console.bluemix.net/docs/services/Cloudant/getting-started.html#getting-started-with-cloudant)
* [Object Storage](https://console.bluemix.net/docs/services/ObjectStorage/index.html)
* [Watson Natural Language Understanding (NLU)](https://www.ibm.com/watson/developercloud/doc/natural-language-understanding)
* [Watson Natural Language Classifier (NLC)](https://console.bluemix.net/docs/services/natural-language-classifier/getting-started.html#natural-language-classifier)
    
## How it works

1. Sources: generic news RSS feeds provided by the user
2. Extraction of keywords and categories for each news using Watson NLU
3. Dataset persisted on Object Storage
4. Creation of a NLC using the generated dataset
5. Classifier training
6. Testing the classifier using _Leave-p-out cross-validation_ 

<img src="NewsifierArchitecture.png" alt="Newsifier Architecture" width="941px" height="529px">

## Prerequisites

* Download and install Eclipse IDE for Java EE Developers from [here](https://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/neon3) 


* Install Liberty server in Eclipse from [here](https://developer.ibm.com/wasdev/downloads/liberty-profile-using-eclipse/)


* Create a new Liberty server in your workspace


* Provision the following services on Bluemix
    * [Cloudant NoSQL DB](https://console.bluemix.net/catalog/services/cloudant-nosql-db)
    * [Object Storage](https://console.bluemix.net/catalog/services/object-storage)
    * [Watson NLU](https://console.bluemix.net/catalog/services/natural-language-understanding)
    * [Watson NLC](https://console.bluemix.net/catalog/services/natural-language-classifier)

	

## Deployment

#### Local machine

Add the credentials for your Bluemix services in _com.newsifier.utils.Credentials_

#### On Bluemix

* Export the application .war file

* Add your services to the _manifest.yml_

* Run the following command from the manifest directory

    
```
cf push -p <exported_application.war> <app_name>
```

Or see [here](https://console.bluemix.net/docs/runtimes/liberty/optionsForPushing.html#options_for_pushing) other options for pushing Liberty applications on Bluemix

## Running the application
```
http://<host>:<port>/newsifier
```

## Authors

* **Simone Rutigliano** - *Software Engineer* - [LinkedIn](https://www.linkedin.com/in/simonerutigliano/)
* **Umberto Manganiello** - *Software Engineer* - [LinkedIn](https://www.linkedin.com/in/umanganiello)