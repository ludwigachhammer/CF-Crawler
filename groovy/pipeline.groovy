def callPost(String urlString, String queryString) {
    def url = new URL(urlString)
    def connection = url.openConnection()
    connection.setRequestMethod("POST")
    connection.doInput = true
    connection.doOutput = true
    connection.setRequestProperty("content-type", "application/json;charset=UTF-8")

    def writer = new OutputStreamWriter(connection.outputStream)
    writer.write(queryString.toString())
    writer.flush()
    writer.close()
    connection.connect()

    new groovy.json.JsonSlurper().parseText(connection.content.text)
}

def callGet(String url) {
    new groovy.json.JsonSlurper().parseText(url.toURL().getText())
}

node {
    
    // ENVIRONMENTAL VARIABLES
	

    deleteDir()

    stage('Sources') {
        checkout([
                $class           : 'GitSCM',
                branches         : [[name: "refs/heads/master"]],
                extensions       : [[$class: 'CleanBeforeCheckout', localBranch: "master"]],
                userRemoteConfigs: [[
                                            credentialsId: 'cbf178fa-56ee-4394-b782-36eb8932ac64',
                                            url          : "https://github.com/Nicocovi/CF-Crawler"
                                    ]]
                ])
    }

    dir("") {
        
        stage("Get Apps-List"){
		withCredentials([[
			     $class          : 'UsernamePasswordMultiBinding',
			     credentialsId   : '98c5d653-dbdc-4b52-81ba-50c2ac04e4f1',
			     usernameVariable: 'CF_USERNAME',
			     passwordVariable: 'CF_PASSWORD'
		]]) {
		sh 'cf login -a https://api.run.pivotal.io -u $CF_USERNAME -p $CF_PASSWORD --skip-ssl-validation'
		sh 'cf target -o ga72hib-org -s masterarbeit'
		APP_LIST = sh (
                	script: 'cf apps',
                	returnStdout: true
            		)
		LENGTH = APP_LIST.length()
            	INDEX = APP_LIST.indexOf("urls", 0)
		APP_SHORTLIST = (APP_LIST.substring(INDEX+4,LENGTH-1)).replace(" ",";").split(";")
		echo "APP_SHORTLIST: ${APP_SHORTLIST}" 	
		}	
        }
		
        stage('Get individual Runtime Info') {
            //TODO
        }
        
        
        stage("Push Documentation"){
            //TODO
        }
        
    }//dir("")

}
