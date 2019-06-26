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
		token = bat(
                	script: 'cf oauth-token',
                	returnStdout: true
            		)
      echo "token: ${token}"
		}
  }	
	
	stage("get apps"){
		def json = "https://api.run.pivotal.io/v2/apps".toURL().getText(requestProperties: [Accept: 'application/json;charset=utf-8', Authorization: 'bearer eyJhbGciOiJSUzI1NiIsImprdSI6Imh0dHBzOi8vdWFhLnJ1bi5waXZvdGFsLmlvL3Rva2VuX2tleXMiLCJraWQiOiJzaGEyLTIwMTctMDEtMjAta2V5IiwidHlwIjoiSldUIn0.eyJqdGkiOiI4ODc1Y2UwZjY5NjU0YWEwOTgwYjBiOGFkNGVkYWJhZSIsInN1YiI6ImYyMjllMTA2LTBiY2MtNGMxMi1hNGQ3LWU1YzM3MzY1N2QwOCIsInNjb3BlIjpbImNsb3VkX2NvbnRyb2xsZXIucmVhZCIsInBhc3N3b3JkLndyaXRlIiwiY2xvdWRfY29udHJvbGxlci53cml0ZSIsIm9wZW5pZCIsInVhYS51c2VyIl0sImNsaWVudF9pZCI6ImNmIiwiY2lkIjoiY2YiLCJhenAiOiJjZiIsImdyYW50X3R5cGUiOiJwYXNzd29yZCIsInVzZXJfaWQiOiJmMjI5ZTEwNi0wYmNjLTRjMTItYTRkNy1lNWMzNzM2NTdkMDgiLCJvcmlnaW4iOiJ1YWEiLCJ1c2VyX25hbWUiOiJsdWR3aWcuYWNoaGFtbWVyQG91dGxvb2suY29tIiwiZW1haWwiOiJsdWR3aWcuYWNoaGFtbWVyQG91dGxvb2suY29tIiwiYXV0aF90aW1lIjoxNTYxNTUxMTYwLCJyZXZfc2lnIjoiNTlhNjQ1ZSIsImlhdCI6MTU2MTU1MTE2NSwiZXhwIjoxNTYxNTUxNzY1LCJpc3MiOiJodHRwczovL3VhYS5ydW4ucGl2b3RhbC5pby9vYXV0aC90b2tlbiIsInppZCI6InVhYSIsImF1ZCI6WyJjbG91ZF9jb250cm9sbGVyIiwicGFzc3dvcmQiLCJjZiIsInVhYSIsIm9wZW5pZCJdfQ.txMM-Qy5K7FrTPJD2OpBl8QL-LSmUwrw5lnICOrecQJhn_vzJalvDuvtNQQULzSl6Nvje57xDJE215Brdhxrc32SascZ_xMtrE4F__XKiRUOOuoUKaTHvbI91jSri5dMhbOaRB781L4OiCwS1C2Ym0LaOuSVjK0KhcGrpxRfnAPvhGT8dsE-QvVhnRLs4DyIowTQXZUfP16ZiMWQSm3W3im3vTw4j9vNhUU3KFj4Zw4FZQujKOBqoxRu5vAbA4p2LqgJT53gYHhpll_m-oOKErgytHHGdvlQydOdIbsmfVL3gHozk2Hvfl_S83Nj7UyK7sKxvCEvxJQFXAMRejlgcQ'])
		echo "Result: ${json}"
	}
}
