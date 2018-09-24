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

node {
    
    // ENVIRONMENTAL VARIABLES
    APP_SHORTLIST = []

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
		APP_SHORTLIST = (APP_LIST.substring(INDEX+5,LENGTH-1)).replaceAll("\\s+",";").split(";")
		echo "APP_SHORTLIST: ${APP_SHORTLIST}" 	
		}	
        }
		
        stage('Get individual Runtime Info') {
            	def iterations = APP_SHORTLIST.size() / 6
		for (i = 0; i <iterations; i++) {
			APP_STATUS = sh (
                	script: 'cf app '+APP_SHORTLIST[0+6*i],
                	returnStdout: true
            		)
			LENGTH = APP_STATUS.length()
			INDEX = APP_STATUS.indexOf("#0", 0)
			APP_SHORTSTATUS = (APP_STATUS.substring(INDEX,LENGTH-1)).replace("   ",";").split(";")
			echo "SHORTSTATUS: ${APP_SHORTSTATUS}"
		}
		
        }
        
        
        stage("Push Documentation"){
		
		def iterations = APP_SHORTLIST.size() / 6
		for (i = 0; i <iterations; i++) {
			def basicinfo = "\"id\": \"XXX\", \"name\": \"${APP_SHORTLIST[0+6*i]}\", \"owner\": \"XXX\", \"description\": \"XXX\", \"short_name\": \"XXX\", \"type\": \"XXX\""
			def additionalinfo = ", \"state\": \"${APP_SHORTLIST[1+6*i]}\", \"url\": \"${APP_SHORTLIST[5+6*i]}\" "
            		def runtime = " \"runtime\": {\"ram\": \"XXX\", \"cpu\": \"XXX\", \"disk\": \"XXX\", \"host_type\": \"cloudfoundry\" }"
            		def jsonstring = "{"+basicinfo+""+additionalinfo+""+runtime+"}"            
            		try {
                    		//callPost("http://192.168.99.100:9123/document", jsonstring) //Include protocol
				echo "POST: ${jsonstring}"
                	} catch(e) {
                   	 // if no try and catch: jenkins prints an error "no content-type" but post request succeed
			}
		}
        }
        
    }//dir("")

}
