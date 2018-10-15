import java.time.*

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
    	APP_SHORTLIST = []
	HOST = "http://192.168.99.100:9123"
	QUERY = "&query={\"query\":{\"match_all\":{}}}"
        
        stage("Get Apps-List"){
		withCredentials([[
			     $class          : 'UsernamePasswordMultiBinding',
			     credentialsId   : '98c5d653-dbdc-4b52-81ba-50c2ac04e4f1',
			     usernameVariable: 'CF_USERNAME',
			     passwordVariable: 'CF_PASSWORD'
		]]) {
		sh 'cf login -a https://api.run.pivotal.io -u $CF_USERNAME -p $CF_PASSWORD --skip-ssl-validation'
		sh 'cf target -o ncorpan-org -s development'
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
			APP_SHORTLIST[1+6*i] = APP_SHORTSTATUS[1]
			//TODO
			//APP_SHORTSTATUS[2]: since date
			//APP_SHORTSTATUS[3]: CPU
			APP_SHORTLIST[3+6*i] = APP_SHORTSTATUS[4] //memory
			APP_SHORTLIST[4+6*i] = APP_SHORTSTATUS[5] //disk
		}
		
        }
        
        
        stage("Push Documentation"){
		LocalDateTime t = LocalDateTime.now();
		def datestring = " { \"date\":\""+t.toString()+"\"} "
		echo "Date: ${datestring}"
		def iterations = APP_SHORTLIST.size() / 6
		for (i = 0; i <iterations; i++) {
			//TODO: get object and only change runtime
			def basicinfo = "\"id\": \"XXX\", \"name\": \"${APP_SHORTLIST[0+6*i]}\", \"owner\": \"XXX\", \"description\": \"XXX\", \"short_name\": \"XXX\", \"type\": \"XXX\""
			def additionalinfo = ", \"status\": \"${APP_SHORTLIST[1+6*i]}\", \"url\": \"${APP_SHORTLIST[5+6*i]}\", \"instances\": \"${APP_SHORTLIST[2+6*i]}\" "
            		def runtime = " \"runtime\": {\"ram\": \"${APP_SHORTLIST[3+6*i]}\", \"cpu\": \"XXX\", \"disk\": \"${APP_SHORTLIST[4+6*i]}\", \"host_type\": \"cloudfoundry\" }"
            		def jsonstring = "{"+basicinfo+""+additionalinfo+""+runtime+"}"            
            		try {
                    		//callPost("http://192.168.99.100:9123/document", jsonstring) //Include protocol
				//callPost("http://192.168.99.100:9123/document", )
                	} catch(e) {
                   		// if no try and catch: jenkins prints an error "no content-type" but post request succeed
			}
		}
        }

}
