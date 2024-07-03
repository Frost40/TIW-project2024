function AlbumPage(pageOrchestrator) {
    this.pageOrchestrator = pageOrchestrator;

    //Getting references of the HTML elements
    let albumPageDiv = document.getElementById("albumPageDiv");
    let goToHomeButton = document.getElementById("homeButtonAlbumPage");
    let albumTitleDiv = document.getElementById("albumTitle");
    let imagesGrid = document.getElementById("albumImages");
    let previousButton = document.getElementById("previousButton");
    let nextButton = document.getElementById("nextButton");
    let orderImageColumn = document.getElementById("orderImageColumn");
    let saveOrderButton = document.getElementById("saveOrder-btn");

    // Dati della pagina
    let images = [];
    let currentPage = 0;
    const imagesPerPage = 5;
    let albumId;
    let imagesWithComments = new Map();

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

    saveOrderButton.addEventListener('click', function() {
        saveOrder();
    });

    // Funzione per aprire la Album page
    this.open = function(albumIdFromHomePage) {
        albumPageDiv.style.display = "";
		albumId = albumIdFromHomePage;
		        console.log(albumId);

        // Construct query string
        var url = "GoToAlbumPage?albumId=" + encodeURIComponent(albumId);

        makeCall("GET", url, null, function(x) {
            if (x.readyState === XMLHttpRequest.DONE) {
                var message = x.responseText;
                switch (x.status) {
                    case 200:
                        var jsonObject = JSON.parse(message);
                        
                        console.log(jsonObject);
                        var albumCreator = jsonObject.creator;
                        var albumTitle = jsonObject.albumTitle;
                        allComments = jsonObject.comments;
                        images = jsonObject.images;
                        
						for (let x of images) {
						    // Controllo se allComments contiene commenti per l'immagine corrente
						    if (allComments.hasOwnProperty(x.id)) {
						        imagesWithComments.set(x, allComments[x.id]);
						    } else {
						        imagesWithComments.set(x, null);
						    }
						}
                        // Estrai le chiavi (nomi delle immagini)
		                console.log(imagesWithComments);
		
                        setAlbumTitle(albumTitle, albumCreator);
                        updateImagesGrid();
                        populateOrderImageColumn(); // Popola la colonna con i titoli delle immagini
                        break;

                    case 400: // bad request
                        pageOrchestrator.showPage("home");
                        showErrorAlert(message);
                        break;

                    case 401: // unauthorized
                        pageOrchestrator.showPage("home");
                        showErrorAlert(message);
                        break;

                    case 500: // server error
                        pageOrchestrator.showPage("home");
                        showErrorAlert(message);
                        break;

                    default:
                        pageOrchestrator.showError(message);
                        break;
                }
            }
        });
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
	            emptySlot.className = "image-slot empty-slot";
	            imagesGrid.appendChild(emptySlot);
	        }
	
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
            img.addEventListener('mouseover', function() {
                pageOrchestrator.showPage("image", albumId, image, imagesWithComments.get(image));
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


    // Funzione per popolare la colonna con i titoli delle immagini
    function populateOrderImageColumn() {
        orderImageColumn.innerHTML = ""; // Pulisci la colonna

        images.forEach(image => {
            let titleItem = document.createElement("div");
            titleItem.className = "title-item";
            titleItem.setAttribute("draggable", "true");
            titleItem.dataset.id = image.id;
            titleItem.textContent = image.title;

            orderImageColumn.appendChild(titleItem);
        });

        let draggedItem = null;
        let placeholder = document.createElement("div");
        placeholder.className = "title-item-placeholder";

        orderImageColumn.addEventListener('dragstart', function(e) {
            draggedItem = e.target;
            e.dataTransfer.setData('text/html', e.target.outerHTML);
            e.dataTransfer.dropEffect = 'move';
            setTimeout(() => {
                draggedItem.classList.add('dragging');
            }, 0);
        });

        orderImageColumn.addEventListener('dragover', function(e) {
            e.preventDefault();
            e.dataTransfer.dropEffect = 'move';

            let target = e.target;
            if (target && target !== draggedItem && target.classList.contains('title-item')) {
                let rect = target.getBoundingClientRect();
                let next = (e.clientY - rect.top) / (rect.bottom - rect.top) > 0.5;
                orderImageColumn.insertBefore(placeholder, next ? target.nextSibling : target);
            }
        });

        orderImageColumn.addEventListener('drop', function(e) {
            e.preventDefault();
            if (draggedItem && placeholder.parentNode) {
                orderImageColumn.insertBefore(draggedItem, placeholder);
                placeholder.remove();
                draggedItem.classList.remove('dragging');
                draggedItem = null;
                updateImageOrder(); // Funzione per aggiornare l'ordine delle immagini
            }
        });

        orderImageColumn.addEventListener('dragend', function() {
            placeholder.remove();
            if (draggedItem) {
                draggedItem.classList.remove('dragging');
            }
            draggedItem = null;
        });
    }

    // Funzione per aggiornare l'ordine delle immagini in base al drag and drop
    function updateImageOrder() {
        let orderedItems = orderImageColumn.getElementsByClassName("title-item");
        images = Array.from(orderedItems).map(item => {
            let id = item.dataset.id;
            return images.find(image => image.id == id);
        });
    }

    // Funzione per salvare l'ordine delle immagini
    function saveOrder() {
        let orderedItems = orderImageColumn.getElementsByClassName("title-item");
        let orderedIds = Array.from(orderedItems).map(item => item.dataset.id);
        
        console.log(orderedIds);
        
        // Creare un form virtuale
        var virtualForm = document.createElement("form");

        // Aggiungere orderedIds come campo nascosto
        var orderedIdsInput = document.createElement("input");
        orderedIdsInput.type = "hidden";
        orderedIdsInput.name = "orderedIds";
        orderedIdsInput.value = JSON.stringify(orderedIds);
        virtualForm.appendChild(orderedIdsInput);

        // Aggiungere albumId come campo nascosto
        var albumIdInput = document.createElement("input");
        albumIdInput.type = "hidden";
        albumIdInput.name = "albumId";
        albumIdInput.value = albumId;
        virtualForm.appendChild(albumIdInput);

        console.log("Form to be sent: ", virtualForm); // Debug log per visualizzare il contenuto del form

        makeCall("POST", "UpdateImageOrder", virtualForm, function(x) {
            if (x.readyState === XMLHttpRequest.DONE) {
                var message = x.responseText;
                switch (x.status) {
                    case 200:
                        showSuccessAlert(message);
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
        albumPageDiv.style.display = "none";
    };
}
