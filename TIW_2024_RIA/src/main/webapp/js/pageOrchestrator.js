document.addEventListener('DOMContentLoaded', () => {
    var user = JSON.parse(sessionStorage.getItem('user'));

    if (!user) {
        window.location.href = "login.html"; // reindirizza alla pagina di login se l'utente non è loggato
    } else {
        var pageOrchestrator = new PageOrchestrator(user);
        pageOrchestrator.showPage('home'); // mostra la sezione homePage all'inizio
    }
});

//It manages the interaction between all the different page
function PageOrchestrator(user){
    //initialize generic useful element
    let actualPage = null;

    //initialiaze all the pages
    let homePage = new HomePage(user, this);
    let albumPage = new AlbumPage(this);
    let imagePage= new ImagePage(this);

    this.logout = function (){
        makeCall("POST", "Logout", null,
        function (x){
                if (x.readyState === XMLHttpRequest.DONE) {
                    let message = x.responseText;
                    switch (x.status) {
                        case 200:
                            sessionStorage.clear();
                            window.location.href = "login.html";
                            break;
                            
                        default:
                            pageOrchestrator.showError(message);
                            break;
                    }
                }
            }
        )
    }

    /*This function is used to show the error page
    * In particular it hides the actual page, and the shows the error message
    * that has just received with a button that allows to come back to the previous page*/
    this.showError = function (message){
        actualPage.hide()
        errorFlag=1
        error.style.display=""
        errormessage.innerHTML=message
        rollback.addEventListener("click",(e)=>{
                errorFlag=0
                error.style.display="none"
                actualPage.openPage()
            }
        )
    }
    
    this.hideActualPage = function() {
      if (actualPage !== null) {
        actualPage.hide();
      }
    }
    
    this.showPage = function (pageName, info1, info2) {
		switch(pageName) {
			case 'home':
				this.hideActualPage();
  				homePage.open();
  				actualPage = homePage;
  				break;
  				
			case 'album':
				this.hideActualPage();
  				albumPage.open(info1);
  				actualPage = albumPage;
				break;
				
		    case 'image':
		      	imagePage.open(info1, info2);
		      	break;
		      	
	      	case 'login':
				this.logout();
				break;
		      	
		    default:
		      	console.log('Pagina non trovata');
		      	break;
		  }
	}
	
	this.closeModal = function() {
        imagePage.closeModal;
    }
	
	/*checks if the session is still active
	* in case if it isn't it shows the HomePage*/
	/*
	window.addEventListener("load", () => {
	    if (user.id == null) {
	        window.location.href = "login.html";
	    } else {
	        //pageOrchestrator.start(); // initialize the components
	    } // display initial content
	}, false);
	*/
	
}


