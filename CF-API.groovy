node {

  stage ("get passcode"){
    def data = new URL("https://login.run.pivotal.io/passcode").getText()
    echo "${data}"
  }                 
}
