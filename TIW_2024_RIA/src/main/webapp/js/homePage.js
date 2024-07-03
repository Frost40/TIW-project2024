function HomePage(user, pageOrchestrator) {
    // Inizializzazione degli elementi
    var self = this;
    this.pageOrchestrator = pageOrchestrator;

    // Elementi generali
    let homePageDiv = document.getElementById("homePageDiv");
    let logoutButton = document.getElementById("logoutButtonHome");
    let usernameSpan = document.getElementById("username");
    
    let myAlbumsContainer = document.getElementById("myAlbumsContainer");
    let othersAlbumsContainer = document.getElementById("othersAlbumsContainer");
    let allUserImagesContainer = document.getElementById("imageList");
    
    let uploadImageForm = document.getElementById("uploadImageForm");
    let createAlbumForm = document.getElementById("createAlbumForm");

    // Setting correct username in the title "h1"
    usernameSpan.textContent = user.username;

    // Aggiunta dei listener agli eventi    
    createAlbumForm.addEventListener('submit', (e) => {
        e.preventDefault();
        createAlbum(e.target); // Passa direttamente l'elemento form
    });
    
    logoutButton.addEventListener('click', function() {
        pageOrchestrator.showPage("login");
    });
    
    uploadImageForm.addEventListener('submit', (e) => {
        e.preventDefault();
        uploadImage(e.target); // Passa direttamente l'elemento form
    });

    // Funzione per gestire l'upload dell'immagine
    function uploadImage(form) {
	    if (form.checkValidity()) {
	        makeCall("POST", 'UploadImage', form, function(x) {
	            if (x.readyState === XMLHttpRequest.DONE) {
	                var message = x.responseText;
	                switch (x.status) {
	                    case 200:
							var jsonObject = JSON.parse(message);
	                        var images = jsonObject.allImages;
	                        
                            self.updateAllUserImages(images);

	                        form.reset();
	                        showSuccessAlert("Image Uploaded successfully!");
	                        break;
	                    
	                    case 400: // bad request
	                        showErrorAlert(message); 
	                        break;
	                    
	                    case 401: // unauthorized
	                        showErrorAlert(message); 
	                        break;
	                    
	                    case 500: // server error
	                        showErrorAlert(message); 
	                        break;
	                    
	                    default:
	                        pageOrchestrator.showError(message);
	                        break;
	                }
	            }
	        });
	    } else {
	        form.reportValidity();
	    }
	}


    // Funzione per aprire la home page
    this.open = function() {
        homePageDiv.style.display = "";
        makeCall("GET", "GoToHomePage", null, 
            function(x) {
                if (x.readyState === XMLHttpRequest.DONE) {
                    var message = x.responseText;
                    switch (x.status) {
                        case 200:
                            var jsonObject = JSON.parse(message);
                            var myAlbums = jsonObject.myAlbums;
                            var othersAlbums = jsonObject.othersAlbums;
                            var allUserImages = jsonObject.allImages;
                            self.updateMyAlbums(myAlbums);
                            self.updateOthersAlbums(othersAlbums);
                            self.updateAllUserImages(allUserImages);
                            break;
                        
                        case 400: // bad request
		                	showErrorAlert(message); 
                            break;
                        
                        case 401: // unauthorized
		                	showErrorAlert(message); 
                            break;
                        
                        case 500: // server error
		                	showErrorAlert(message); 
                            break;
                        
                        default:
                            pageOrchestrator.showError(message);
                            break;
                    }
                }
            }
        );
    };

    // Funzione per aggiornare la lista degli album
    this.updateMyAlbums = function (myAlbums) {    
        myAlbumsContainer.innerHTML = '';

        if (myAlbums === null)		return;

        myAlbums.forEach(function(album, index) {
            var albumElement = document.createElement('div');
            albumElement.className = 'album';
        
            var albumLink = document.createElement('a');
        
            var albumIcon = document.createElement('img');
            albumIcon.src = index === 0 ? 'Icons/album0.png' : 'Icons/album.png';
            albumIcon.className = 'album-icon';
        
            var albumTitle = document.createElement('h3');
            albumTitle.textContent = album.title;
            albumTitle.className = 'album-title';
        
            // Aggiungo listener all'icona
            albumIcon.addEventListener('click', function() {
                pageOrchestrator.showPage("album", album.id, null);
            });
        
            // Aggiungo listener al titolo
            albumTitle.addEventListener('click', function() {
                pageOrchestrator.showPage("album", album.id, null);
            });
        
            albumLink.appendChild(albumIcon);
            albumLink.appendChild(albumTitle);
            albumElement.appendChild(albumLink);
            myAlbumsContainer.appendChild(albumElement);
        });
    }
    
    // Funzione per aggiornare la lista degli album degli altri utenti
    this.updateOthersAlbums = function (othersAlbums) {    
        // Rimuovo eventuali elementi precedenti
        othersAlbumsContainer.innerHTML = '';
        
        if (othersAlbums === null)		return;
    
        if (othersAlbums.length === 0) {
            var noAlbumsElement = document.createElement('div');
            noAlbumsElement.className = 'no-albums';
            var noAlbumsText = document.createElement('p');
            noAlbumsText.textContent = 'There are no other albums to show';
            noAlbumsElement.appendChild(noAlbumsText);
            othersAlbumsContainer.appendChild(noAlbumsElement);
        } else {
            othersAlbums.forEach(function(album) {
                var albumElement = document.createElement('div');
                albumElement.className = 'album';
        
                var albumLink = document.createElement('a');
        
                var albumIcon = document.createElement('img');
                albumIcon.src = 'Icons/album.png';
                albumIcon.className = 'album-icon';
        
                var albumTitle = document.createElement('h3');
                albumTitle.textContent = album.title;
                albumTitle.className = 'album-title';
        
                var albumUser = document.createElement('p');
                albumUser.textContent = 'by ' + album.userId;
                albumUser.className = 'album-user';
        
                // Aggiungo listener all'icona e al titolo
                albumIcon.addEventListener('click', function() {
                    pageOrchestrator.showPage("album", album.id);
                });
        
                albumTitle.addEventListener('click', function() {
                    pageOrchestrator.showPage("album", album.id);
                });
        
                albumLink.appendChild(albumIcon);
                albumLink.appendChild(albumTitle);
                albumLink.appendChild(albumUser);
                albumElement.appendChild(albumLink);
                othersAlbumsContainer.appendChild(albumElement);
            });
        }
    }
    
    // Funzione per aggiornare la lista delle immagini degli utenti
    this.updateAllUserImages = function (images) {    
        // Rimuovo eventuali elementi precedenti
        allUserImagesContainer.innerHTML = '';
    
        images.forEach(function(image) {
            var imageRow = document.createElement('div');
            imageRow.className = 'image-row';
            
            var checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.id = 'image-' + image.id;
            checkbox.name = 'selectedImages';
            checkbox.value = image.id;
            checkbox.className = 'image-checkbox';
            imageRow.appendChild(checkbox);
            
            var imageInfo = document.createElement('div');
            imageInfo.className = 'image-info';
            
            var imageTitle = document.createElement('span');
            imageTitle.textContent = image.title;
            imageTitle.className = 'image-title';
            imageInfo.appendChild(imageTitle);
            
            var imageId = document.createElement('span');
            imageId.textContent = image.id;
            imageId.className = 'image-id';
            imageInfo.appendChild(imageId);
            
            imageRow.appendChild(imageInfo);
            allUserImagesContainer.appendChild(imageRow);
        });
    }
        
    // Funzione per creare l'album
function createAlbum(form) {
    var selectedImages = [];
    var checkboxes = allUserImagesContainer.querySelectorAll('input[type="checkbox"]');
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i].checked) {
            selectedImages.push(checkboxes[i].value);
            console.log(checkboxes[i].value);
        }
    }

    // Creare un form virtuale
    var virtualForm = document.createElement("form");
    // Aggiungere tutti gli input dal form originale al form virtuale
    for (var i = 0; i < form.elements.length; i++) {
        var field = form.elements[i];
        if (field.name && field.value) {
            var input = document.createElement("input");
            input.type = "hidden";
            input.name = field.name;
            input.value = field.value;
            virtualForm.appendChild(input);
        }
    }

    // Rimuovere gli input esistenti 'selectedImages' dal form virtuale
    var existingSelectedImagesInputs = virtualForm.querySelectorAll('input[name="selectedImages"]');
    existingSelectedImagesInputs.forEach(function(input) {
        virtualForm.removeChild(input);
    });

    // Aggiungere selectedImages come campo nascosto
    var selectedImagesInput = document.createElement("input");
    selectedImagesInput.type = "hidden";
    selectedImagesInput.name = "selectedImages";
    selectedImagesInput.value = JSON.stringify(selectedImages);
    virtualForm.appendChild(selectedImagesInput);
    
    console.log(virtualForm);

    makeCall("POST", 'CreateNewAlbum', virtualForm, function(x) {
        if (x.readyState === XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
                case 200:
					var jsonObject = JSON.parse(message);
                    var albums = jsonObject.albums;
                    console.log(albums);
                    
                    self.updateMyAlbums(albums);
                            
                    form.reset();
                    showSuccessAlert("Album created successfully!");
                    break;

                case 400: // bad request
                    showErrorAlert(message);
                    break;

                case 401: // unauthorized
                    showErrorAlert(message);
                    break;

                case 500: // server error
                    showErrorAlert(message);
                    break;

                default:
                    pageOrchestrator.showError(message);
                    break;
            }
        }
    });
}


    // Funzione per nascondere la pagina
    this.hide = function() {
        homePageDiv.style.display = "none";
    };
}