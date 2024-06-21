function AlbumPage(pageOrchestrator) {
	var self = this;
    this.pageOrchestrator = pageOrchestrator;
    
    //Getting references of the HTML elements
    let albumPageDiv = document.getElementById("albumPageDiv");
	let goToHomeButton = document.getElementById("homeButtonAlbumPage");
	let albumTitleDiv = document.getElementById("albumTitle");
	let imagesGrid = document.getElementById("albumImages");
    let previousButton = document.getElementById("previousButton");
    let nextButton = document.getElementById("nextButton");

	// Dati della pagina
    let images = [];
    let currentPage = 0;
    const imagesPerPage = 5;
    let albumId = null;
    
	//Adding listeners
	goToHomeButton.addEventListener('click', function() {
	    pageOrchestrator.showPage("home");
	});
	
	previousButton.addEventListener('click', function() {
        if (currentPage > 0) {
            currentPage--;
            updateImagesGrid();
        }
    });

    nextButton.addEventListener('click', function() {
        if ((currentPage + 1) * imagesPerPage < images.length) {
            currentPage++;
            updateImagesGrid();
        }
    });

	// Funzione per aprire la Album page
	this.open = function(albumId) {
	    albumPageDiv.style.display = "";
	    console.log(albumId);
	
	    /// Construct query string
    	var url = "GoToAlbumPage?albumId=" + encodeURIComponent(albumId);
	    	
	    makeCall("GET", url, null, 
	        function(x) {
	            if (x.readyState === XMLHttpRequest.DONE) {
	                var message = x.responseText;
	                switch (x.status) {
	                    case 200:
	                        var jsonObject = JSON.parse(message);
	                        var albumCreator = jsonObject.creator;
	                        var albumTitle = jsonObject.albumTitle;
	                        images = jsonObject.images;
	
	                        setAlbumTitle(albumTitle, albumCreator);
	                        updateImagesGrid();
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

	
	function setAlbumTitle(albumTitle, creator) {
	  	albumTitleDiv.textContent = "Album " + albumTitle + ", created by " + creator;
	}
	
	function updateImagesGrid() {
	    imagesGrid.innerHTML = ""; // Cleaning the grid
	
	    if (images === null || images.length === 0) {
	        // Aggiungi 5 slot vuoti se non ci sono immagini
	        for (let i = 0; i < imagesPerPage; i++) {
	            let emptySlot = document.createElement("div");
	            emptySlot.className = "image-slot empty";
	            imagesGrid.appendChild(emptySlot);
	        }
	
	        // Nascondi i pulsanti di navigazione se non ci sono immagini
	        previousButton.style.display = "none";
	        nextButton.style.display = "none";
	        return;
	    }
	
	    let start = currentPage * imagesPerPage;
	    let end = Math.min(start + imagesPerPage, images.length);
	
	    for (let i = start; i < end; i++) {
	        let image = images[i];
	        let imageSlot = document.createElement("div");
	        imageSlot.className = "image-slot";
	
	        let imageLink = document.createElement("a");
	        imageLink.href = `javascript:void(0);`; // Prevent default link behavior
	
	        let img = document.createElement("img");
	        img.src = image.path;
	        img.alt = "Thumbnail";
	        img.className = "thumbnail";
	
	        let p = document.createElement("p");
	        p.textContent = image.title;
	
	        // Aggiungi listener per l'immagine e il titolo
	        imageLink.addEventListener('click', function() {
	            pageOrchestrator.showPage("image", albumId, image.id);
	        });
	        img.addEventListener('click', function() {
	            pageOrchestrator.showPage("image", albumId, image.id);
	        });
	        p.addEventListener('click', function() {
	            pageOrchestrator.showPage("image", albumId, image.id);
	        });
	
	        imageLink.appendChild(img);
	        imageLink.appendChild(p);
	        imageSlot.appendChild(imageLink);
	        imagesGrid.appendChild(imageSlot);
	    }
	
	    // Aggiungi slot vuoti se ci sono meno di 5 immagini
	    for (let i = end; i < start + imagesPerPage; i++) {
	        let emptySlot = document.createElement("div");
	        emptySlot.className = "image-slot empty";
	        imagesGrid.appendChild(emptySlot);
	    }
	
	    // Aggiorna la visibilità dei pulsanti di navigazione
	    previousButton.style.display = currentPage > 0 ? "inline-block" : "none";
	    nextButton.style.display = end < images.length ? "inline-block" : "none";
	}

	// Funzione per nascondere la pagina
    this.hide = function() {
        albumPageDiv.style.display = "none";
    };
}