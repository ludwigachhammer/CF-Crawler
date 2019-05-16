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
	List<String> APP_LONGLIST = new ArrayList<String>()
	HOST = "http://localhost:8080/"
	QUERY = "query={\"query\":{\"match_all\":{}}}"
	FIELDS = "fields=id,name&"
	List<String> PIVIO_APPS = new ArrayList<String>()
	String encodedQuery = URLEncoder.encode(QUERY, "UTF-8");
	echo "http://localhost:8080//document?+${FIELDS}+${encodedQuery}";
	//String url = "http://localhost:8080//document?"+FIELDS+encodedQuery
	String url = "http://localhost:8080//document/"
	
	
	stage("Get Pivio-Apps"){
		echo "URL: ${url}"
		def PIVIO_APPS_TMP = callGet(url)
		for (i = 0; i <PIVIO_APPS_TMP.size(); i++) {
			PIVIO_APPS.add(PIVIO_APPS_TMP[i].id)
			PIVIO_APPS.add(PIVIO_APPS_TMP[i].name)
		}
		//echo "PIVIO_APPS_TMP: ${PIVIO_APPS_TMP}"
		echo "PIVIO_APPS: ${PIVIO_APPS}"
		//echo "PIVIO_APPS SIZE: ${PIVIO_APPS.size()}"
        }
        
        stage("Get Apps-List"){
		withCredentials([[
			     $class          : 'UsernamePasswordMultiBinding',
			     credentialsId   : '05487704-f456-43cb-96c3-72aaffdba62f',
			     usernameVariable: 'CF_USERNAME',
			     passwordVariable: 'CF_PASSWORD'
		]]) {
		bat "cf login -a https://api.run.pivotal.io -u $CF_USERNAME -p \"$CF_PASSWORD\" --skip-ssl-validation"
		bat 'cf target -o ead-tool -s development'
		APP_LIST = bat(
                	script: 'cf apps',
                	returnStdout: true
            		)
		echo "APP_LIST: ${APP_LIST}"
		LENGTH = APP_LIST.length()
            	INDEX = APP_LIST.indexOf("urls", 0)
		APP_SHORTLIST = (APP_LIST.substring(INDEX+5,LENGTH-1)).replaceAll("\\s+",";").split(";")
		echo "APP_SHORTLIST: ${APP_SHORTLIST}"
		}	
        }
		
        stage('Get individual Runtime Info') {
            	def iterations = APP_SHORTLIST.size() / 6
		for (i = 0; i <iterations; i++) {
			APP_STATUS = bat (
                	script: 'cf app '+APP_SHORTLIST[0+6*i],
                	returnStdout: true
            		)
			LENGTH = APP_STATUS.length()
			INDEX = APP_STATUS.indexOf("#0", 0)
			echo "APP_STATUS: ${APP_STATUS}"
			APP_SHORTSTATUS = (APP_STATUS.substring(INDEX,LENGTH-1)).replaceAll("\n"," ").replaceAll("  \\s+",";").split(";")
			echo "SHORTSTATUS: ${APP_SHORTSTATUS}"
			APP_SHORTLIST[1+6*i] = APP_SHORTSTATUS[1]
			APP_SHORTSTATUS[5] = APP_SHORTSTATUS[5].replace("type:", "")
			APP_SHORTLIST[3+6*i] = APP_SHORTSTATUS[4] //memory
			APP_SHORTLIST[4+6*i] = APP_SHORTSTATUS[5] //disk
			//TODO
			echo "PIVIO_APP: ${PIVIO_APPS}"
			echo "APP_SHORTLIST: ${APP_SHORTLIST}"
			if(PIVIO_APPS.contains(APP_SHORTLIST[0+6*i])){
				def index = PIVIO_APPS.indexOf(APP_SHORTLIST[0+6*i])
				APP_LONGLIST.add(PIVIO_APPS.get(index-1)) //id
				echo "App: ${APP_SHORTLIST[0]}"
				echo "Element: ${PIVIO_APPS[index]} , ${PIVIO_APPS[index-1]}"
			}else{
				APP_LONGLIST.add("XXX") //id
			}
			APP_LONGLIST.add(APP_SHORTLIST[0+6*i]) //0 name
			APP_LONGLIST.add(APP_SHORTLIST[2+6*i]) //2 instances
			APP_LONGLIST.add(APP_SHORTLIST[5+6*i]) //5 url
			APP_LONGLIST.add(APP_SHORTSTATUS[1]) //1 status
			APP_LONGLIST.add(APP_SHORTSTATUS[2]) //2 since date
			APP_LONGLIST.add(APP_SHORTSTATUS[3]) //3 CPU
			APP_LONGLIST.add(APP_SHORTSTATUS[4]) //4 memory
			APP_LONGLIST.add(APP_SHORTSTATUS[5]) //5 disk
			
		}
		echo "APP_LONGLIST: ${APP_LONGLIST}"
        }
        
        
        stage("Push Documentation"){
		//TIME
		LocalDateTime t = LocalDateTime.now();
		tAsString = t.toString()
		formatted = (t.toString()).replace("T", " ")
		formatted = formatted.substring(0,(formatted.length())-4)
		def datestring = " { \"date\":\""+formatted+"\"} "
		def APPS_TO_UPDATE = APP_LONGLIST.toArray();
		//lastdeployment = APPS_TO_UPDATE[5+9*i]
		//TODO:
		//BUILD JSON
		def iterations = APPS_TO_UPDATE.size() / 9
		for (i = 0; i <iterations; i++) {
			//TODO: get object and only change runtime
			def basicinfo = " \"id\":\"${APPS_TO_UPDATE[0+9*i]}\",\"name\": \"${APPS_TO_UPDATE[1+9*i]}\","
			def additionalinfo = " \"status\": \"${APPS_TO_UPDATE[4+9*i]}\", \"url\": \"${APPS_TO_UPDATE[3+9*i]}\", \"lastUpdate\":\"${tAsString}\", "
            		def runtime = " \"runtime\": {\"ram\": \"${APPS_TO_UPDATE[7+9*i]}\", \"cpu\": \"${APPS_TO_UPDATE[6+9*i]}\", \"disk\": \"${APPS_TO_UPDATE[8+9*i]}\", \"instances\": \"${APPS_TO_UPDATE[2+9*i]}\", \"host_type\": \"cloudfoundry\" }"
            		def jsonstring = "{"+basicinfo+""+additionalinfo+""+runtime+"}"
			echo "JSONString: ${jsonstring}"
			def flatJSON = "{\"id\":\"${APPS_TO_UPDATE[0+9*i]}\",\"name\": \"${APPS_TO_UPDATE[1+9*i]}\", \"status\": \"${APPS_TO_UPDATE[4+9*i]}\", \"url\": \"${APPS_TO_UPDATE[3+9*i]}\", \"lastUpdate\":\"${tAsString}\", \"ram\": \"${APPS_TO_UPDATE[7+9*i]}\", \"cpu\": \"${APPS_TO_UPDATE[6+9*i]}\", \"disk\": \"${APPS_TO_UPDATE[8+9*i]}\", \"instances\": \"${APPS_TO_UPDATE[2+9*i]}\", \"host_type\": \"cloudfoundry\"}"
			echo "flatJSON: ${flatJSON}" 
			try{
				//callPost("http://192.168.99.100:8080/update/microservice", jsonstring)
				//def test = new JsonSlurper().parseText(flatJson)
				if (flatJSON.id=="") {
					echo "${i}. call to endpoint //document: localhost:8080/document"
					callPost("http://localhost:8080/document/", flatJSON)
				}
				else
					echo "${i}. call to endpoint //update: localhost:8025/update/microservice"
					callPost("http://localhost:8025/update/microservice", jsonstring)
			}catch(e){
				echo "Exception: ${e}"
			}
		}
		try {
			//callPost("http://192.168.99.100:8080/endpoint/lastUpdateOfCrawler", datestring)
			callPost("http://localhost:8025/endpoint/lastUpdateOfCrawler", datestring)
		} catch(e) {
			// if no try and catch: jenkins prints an error "no content-type" but post request succeed
		}
        }

}
