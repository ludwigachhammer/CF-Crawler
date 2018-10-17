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
	HOST = "http://192.168.99.100:9123"
	QUERY = "query={\"query\":{\"match_all\":{}}}"
	FIELDS = "fields=id,name&"
	List<String> PIVIO_APPS = new ArrayList<String>()
	String encodedQuery = URLEncoder.encode(QUERY, "UTF-8");
	String url = "http://192.168.99.100:9123//document?"+FIELDS+encodedQuery
	
	
	stage("Get Pivio-Apps"){
		echo "URL: ${url}"
		def PIVIO_APPS_TMP = callGet(url)
		for (i = 0; i <PIVIO_APPS_TMP.size(); i++) {
			PIVIO_APPS.add(PIVIO_APPS_TMP[i].id)
			PIVIO_APPS.add(PIVIO_APPS_TMP[i].name)
		}
		echo "PIVIO_APPS_TMP: ${PIVIO_APPS_TMP}"
		echo "PIVIO_APPS: ${PIVIO_APPS}"
		echo "PIVIO_APPS SIZE: ${PIVIO_APPS.size()}"
        }
        
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
			APP_STATUS = sh (
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
			if(PIVIO_APPS.contains(APP_SHORTLIST[0])){
				dex index = PIVIO_APPS.indexOf(APP_SHORTLIST[0])
				APP_LONGLIST.add(PIVIO_APPS.get(index-1)) //id
			}else{
				APP_LONGLIST.add("XXX") //id
			}
			APP_LONGLIST.add(APP_SHORTLIST[0]) //name
			APP_LONGLIST.add(APP_SHORTSTATUS[1]) //status
			APP_LONGLIST.add(APP_SHORTSTATUS[2]) //since date
			APP_LONGLIST.add(APP_SHORTSTATUS[3]) //CPU
			APP_LONGLIST.add(APP_SHORTSTATUS[4]) //memory
			APP_LONGLIST.add(APP_SHORTSTATUS[5]) //disk
			APP_LONGLIST.add(APP_SHORTLIST[2]) //instances
			APP_LONGLIST.add(APP_SHORTLIST[5]) //url
		}
		echo "APP_LONGLIST: ${APP_LONGLIST}"
        }
        
        
        stage("Push Documentation"){
		//TIME
		LocalDateTime t = LocalDateTime.now();
		formatted = (t.toString()).replace("T", " ")
		formatted = formatted.substring(0,(formatted.length())-4)
		def datestring = " { \"date\":\""+formatted+"\"} "
		echo "APP_SHORTLIST: ${APP_SHORTLIST}"
		//TODO:
		//BUILD JSON
		def iterations = APP_SHORTLIST.size() / 6
		for (i = 0; i <iterations; i++) {
			//TODO: get object and only change runtime
			def basicinfo = " \"id\":XXX\" \",\"name\": \"${APP_SHORTLIST[0+6*i]}\","
			def additionalinfo = " \"status\": \"${APP_SHORTLIST[1+6*i]}\", \"url\": \"${APP_SHORTLIST[5+6*i]}\", \"instances\": \"${APP_SHORTLIST[2+6*i]}\", "
            		def runtime = " \"runtime\": {\"ram\": \"${APP_SHORTLIST[3+6*i]}\", \"cpu\": \"XXX\", \"disk\": \"${APP_SHORTLIST[4+6*i]}\", \"host_type\": \"cloudfoundry\" }"
            		def jsonstring = "{"+basicinfo+""+additionalinfo+""+runtime+"}"
			echo "JSONString: ${jsonstring}"
            		try {
				callPost("http://localhost:8080/update/microservice", jsonstring)
				callPost("http://localhost:8080/endpoint/lastUpdateOfCrawler", datestring)
                	} catch(e) {
                   		// if no try and catch: jenkins prints an error "no content-type" but post request succeed
			}
		}
		try {
			callPost("http://localhost:8080/endpoint/lastUpdateOfCrawler", datestring)
		} catch(e) {
			// if no try and catch: jenkins prints an error "no content-type" but post request succeed
		}
        }

}
