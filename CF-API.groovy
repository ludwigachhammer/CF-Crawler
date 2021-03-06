//import groovy.json.JsonSlurper
//import groovyx.net.http.HTTPBuilder


node {


  stage("login"){
		withCredentials([[
			     $class          : 'UsernamePasswordMultiBinding',
			     credentialsId   : '05487704-f456-43cb-96c3-72aaffdba62f',
			     usernameVariable: 'CF_USERNAME',
			     passwordVariable: 'CF_PASSWORD'
		]]) {
		bat "cf login -a https://api.run.pivotal.io -u $CF_USERNAME -p \"$CF_PASSWORD\" --skip-ssl-validation"
		bat 'cf target -o ead-tool -s development'
		}	
  }
  
	
  stage ("get passcode"){
    //def data = new URL("https://login.run.pivotal.io/passcode").getText()
    //echo "${data}"
    
    withCredentials([[
			     $class          : 'UsernamePasswordMultiBinding',
			     credentialsId   : '05487704-f456-43cb-96c3-72aaffdba62f',
			     usernameVariable: 'CF_USERNAME',
			     passwordVariable: 'CF_PASSWORD'
		]]) {
		result = bat(
                	script: 'cf oauth-token',
                	returnStdout: true
            		)
      echo "token: ${result}"
	String token = result.split('\n') // Split into an array based on newline
                     .drop(2)     // Drop the first element
                     .join('\n')  // Join back into a string separated by newline
	    echo "token: ${token}"

		def apiUrl = new URL("https://api.run.pivotal.io/v2/apps")
		def json = apiUrl.toText(requestProperties: [Accept: 'application/json;charset=utf-8', Authorization: "${token}"])
		echo "Result: ${json}"

		/*
		def apiString = "https://api.run.pivotal.io/v2/apps"
		def json = new JsonSlurper().parse(apiString)
		echo "Result: ${json}"
		*/

		//getApps("${token}")


		}
  }	
	/*
	stage("get apps"){
		def json = "https://api.run.pivotal.io/v2/apps".toURL().getText(requestProperties: [Accept: 'application/json;charset=utf-8', Authorization: 'bearer eyJhbGciOiJSUzI1NiIsImprdSI6Imh0dHBzOi8vdWFhLnJ1bi5waXZvdGFsLmlvL3Rva2VuX2tleXMiLCJraWQiOiJzaGEyLTIwMTctMDEtMjAta2V5IiwidHlwIjoiSldUIn0.eyJqdGkiOiI1OWY1NjI5YjRmMDM0OTNmOTk0YjE0MmY4MWZkMDk3NCIsInN1YiI6ImYyMjllMTA2LTBiY2MtNGMxMi1hNGQ3LWU1YzM3MzY1N2QwOCIsInNjb3BlIjpbImNsb3VkX2NvbnRyb2xsZXIucmVhZCIsInBhc3N3b3JkLndyaXRlIiwiY2xvdWRfY29udHJvbGxlci53cml0ZSIsIm9wZW5pZCIsInVhYS51c2VyIl0sImNsaWVudF9pZCI6ImNmIiwiY2lkIjoiY2YiLCJhenAiOiJjZiIsImdyYW50X3R5cGUiOiJwYXNzd29yZCIsInVzZXJfaWQiOiJmMjI5ZTEwNi0wYmNjLTRjMTItYTRkNy1lNWMzNzM2NTdkMDgiLCJvcmlnaW4iOiJ1YWEiLCJ1c2VyX25hbWUiOiJsdWR3aWcuYWNoaGFtbWVyQG91dGxvb2suY29tIiwiZW1haWwiOiJsdWR3aWcuYWNoaGFtbWVyQG91dGxvb2suY29tIiwiYXV0aF90aW1lIjoxNTYxNTUxNDU5LCJyZXZfc2lnIjoiNTlhNjQ1ZSIsImlhdCI6MTU2MTU1MTQ2NCwiZXhwIjoxNTYxNTUyMDY0LCJpc3MiOiJodHRwczovL3VhYS5ydW4ucGl2b3RhbC5pby9vYXV0aC90b2tlbiIsInppZCI6InVhYSIsImF1ZCI6WyJjbG91ZF9jb250cm9sbGVyIiwicGFzc3dvcmQiLCJjZiIsInVhYSIsIm9wZW5pZCJdfQ.H-74peHg7DbdLEHnudHDA091ynC6qqD6twuR3CTimxYsNPp6qxr_Y-Od7eT4WxyUrMzkwCIdriEKnGtIUvTxKfecaxGL8Uk6Albc8G5oyPx-R0T_0xd1p4N12GIUUraivB7e7bxaXzjEvjFuVxDRlwFfVQrqzPx6hAvjo1VrCxcVix6fN8A8tKPWaheXfh9l4KSTE_i51o7R4mU4LzKJFrofJMyakBuxvCvKaVtxKsPmecY43qgvO6B2dlxHhBNdZt05VlJA4pYNriv8d6EFxdmhBiiEKCRSOn7zxLCH5Wo8XhE8kDdXqfCElGKG50AQEJUdYMZzKLC6H0TC1ShG6A'])
		echo "Result: ${json}"
	}*/
}

/*
def make_get_request(String token) {

	def http = new HTTPBuilder()

	http.request( 'https://api.run.pivotal.io', Method.GET, ContentType.JSON ) { req ->

		uri.path = '/v2/apps'
		headers.Authorization = "${token}"
		headers.Accept = 'application/json;charset=utf-8'

		response.success = { resp, reader ->
			assert resp.statusLine.statusCode == 200
			println "Got response: ${resp.statusLine}"
			println "Content-Type: ${resp.headers.'Content-Type'}"
			println reader.text
		}

		response.'404' = {
			println 'Not found'
		}
	}
}
*/

/*
def getApps(String token) {
	def url = new URL('https://api.run.pivotal.io/v2/apps')
	def connection = url.openConnection()
	connection.setRequestMethod = 'GET'
	myURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
	connection.setRequestProperty("Accept", "application/json;charset=utf-8")
	connection.setRequestProperty("Authorization", "${token}")
	if (connection.responseCode == 200) {
		echo "${connection.content.text}"
		println connection.content.text
		println connection.contentType
		println connection.lastModified
		connection.headerFields.each { println "> ${it}" }
	} else {
		println "fucked up"
	}
}*/