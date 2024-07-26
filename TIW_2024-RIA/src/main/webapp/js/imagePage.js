function ImagePage(user, pageOrchestrator) {
    var self = this;
    this.pageOrchestrator = pageOrchestrator;

    // Getting references of the HTML elements
    let modal = document.getElementById("imageModal");
    let imageModalContent = document.getElementById("imageModalContent");
    let imagePageDiv = document.getElementById("albumPageDiv");
    let imageContainer = document.getElementById("fullImage");
    let imageTitleHeader = document.getElementById("imageTitleHeader");
    let imageCreationDate = document.getElementById("imageCreationDate");
    let imageDescription = document.getElementById("imageDescriptionImagePage");
    let commentsWrapper = document.getElementById("commentsWrapper");
    let addCommentForm = document.getElementById("addCommentForm");
    let deleteImageForm = document.getElementById("deleteImageForm");

    // Data of the page
    let currentUser = user;
    let image;
    let albumId;
    let imageComments;

    // Adding listeners
    document.getElementById("alert-button").addEventListener("click", hideAlert);

    addCommentForm.addEventListener('submit', (e) => {
        e.preventDefault();
        addComment(e.target); // Passa direttamente l'elemento form
    });

    deleteImageForm.addEventListener('submit', (e) => {
        e.preventDefault();
        deleteImage(e.target); // Passa direttamente l'elemento form
    });

    // Function to open the Image page
    this.open = function(albumIdFromAlbumPage, imageFromAlbumPage, imageCommentsFromAlbum) {
        image = imageFromAlbumPage;
        albumId = albumIdFromAlbumPage;
        imageComments = imageCommentsFromAlbum;
        
        if(image === null)	return;
        
        setImage(image);
        setComments(imageComments);
        
		if (currentUser.id === image.userId)		setImageInfo(image, true);
		else		setImageInfo(image, false);
        
        self.openModal();

        // Add mouseleave event listener to close modal when mouse leaves imageModalContent
        imageModalContent.addEventListener('mouseleave', self.closeModalOnMouseLeave);
    };

    function setImage(image) {
        imageContainer.src = image.path;
        imageContainer.alt = "Image";
    }

    function setImageInfo(image, canBeDeleted) {    
        imageTitleHeader.textContent = image.title;
        imageCreationDate.textContent = "Created on: " + image.creationDate;
        imageDescription.textContent = "Description: " + image.description;

        if (canBeDeleted) {
            deleteImageForm.style.display = "block";
            deleteImageForm.querySelector('input[name="imageId"]').value = image.id;
        } else {
            deleteImageForm.style.display = "none";
        }
    }

    function setComments(comments) {
		commentsWrapper.innerHTML = "";

		if (comments === null)	return;
	
	    comments.forEach(comment => {
	        let p = document.createElement("p");
	        p.textContent = `${comment.key}: ${comment.value}`; // Use textContent instead of innerHTML
	        commentsWrapper.appendChild(p);
	    });
	}

    function addComment(form) {
	    let comment = form.elements["comment"].value.trim(); // Ottieni il valore del commento e rimuovi spazi bianchi
	
	    if (comment === "") {
	        showAlert("The comment can not be empty");
	        return; // Esci dalla funzione senza fare ulteriori operazioni
	    }
	
	    // Se il commento non è vuoto, procedi con l'invio della richiesta al server
	    let formData = new FormData();
	    formData.append("comment", comment);
	    formData.append("imageId", image.id);
	
	    let tempForm = document.createElement("form");
	
	    // Aggiungi i campi FormData al form temporaneo
	    for (let [key, value] of formData.entries()) {
	        let input = document.createElement("input");
	        input.type = "hidden";
	        input.name = key;
	        input.value = value;
	        tempForm.appendChild(input);
	    }
	
	    makeCall("POST", "AddComment", tempForm, function(x) {
	        if (x.readyState === XMLHttpRequest.DONE) {
	            var message = x.responseText;
	            switch (x.status) {
	                case 200:
	                    showAlert("Commento aggiunto con successo!");
	                    var jsonObject = JSON.parse(message);
	                    var comments = jsonObject.comments;
	                    setComments(comments);
	                    form.elements["comment"].value = ""; // Cancella il campo di input del commento
	                    break;
	
	                case 400: // bad request
	                    showAlert(message);
	                    break;
	
	                case 401: // unauthorized
	                    showAlert(message);
	                    break;
	
	                case 500: // server error
	                    showAlert(message);
	                    break;
	
	                default:
                    	pageOrchestrator.showPage("login");
	                    break;
	            }
	        }
	    });
	}

    function deleteImage(form) {	
	    if (form.checkValidity()) {
		    makeCall("POST", "DeleteImage", form, function(x) {
		        if (x.readyState === XMLHttpRequest.DONE) {
		            var message = x.responseText;
		            
		            switch (x.status) {
		                case 200:
		                    showSuccessAlert(message);
		                    pageOrchestrator.showPage("album", albumId);
		                    break;
		
		                case 400: // bad request
		                    showAlert(message);
		                    break;
		
		                case 401: // unauthorized
		                    showAlert(message);
		                    break;
		
		                case 500: // server error
		                    showAlert(message);
		                    break;
		
		                default:
                    		pageOrchestrator.showPage("login");
		                    break;
		            }
		        }
		    });
	    } else {
	        form.reportValidity();
	    }
	}


    // Function to open the modal
    this.openModal = function() {
        modal.style.display = "block";
    };

    // Function to close the modal
    this.closeModal = function() {
        modal.style.display = "none";
    };

    // Function to close modal when mouse leaves imageModalContent
    this.closeModalOnMouseLeave = function(event) {
        if (!imageModalContent.contains(event.relatedTarget)) {
            self.closeModal();
            imageModalContent.removeEventListener('mouseleave', self.closeModalOnMouseLeave);
        }
        
		pageOrchestrator.showPage("album", albumId);
    };

    // Function to hide the page
    this.hide = function() {
        imagePageDiv.style.display = "none";
    };
    
    function showAlert(message) {
	    let alertBox = document.getElementById("alert-box");
	    let alertMessage = alertBox.querySelector(".alert-message");
	    alertMessage.textContent = message;
	    alertBox.style.display = "block";
	}
	
	function hideAlert() {
	    let alertBox = document.getElementById("alert-box");
	    alertBox.style.display = "none";
	}

}
