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
}
