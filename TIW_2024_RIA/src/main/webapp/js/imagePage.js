function ImagePage(pageOrchestrator) {
    var self = this;
    this.pageOrchestrator = pageOrchestrator;

    // Getting references of the HTML elements
    let modal = document.getElementById("imageModal");
    let imageModalContent = document.getElementById("imageModalContent");
    let imageContainer = document.getElementById("fullImage");
    let imageTitleHeader = document.getElementById("imageTitleHeader");
    let imageCreationDate = document.getElementById("imageCreationDate");
    let imageDescription = document.getElementById("imageDescription");
    let commentsWrapper = document.getElementById("commentsWrapper");
    let addCommentForm = document.getElementById("addCommentForm");
    let deleteImageForm = document.getElementById("deleteImageForm");

    // Data of the page
    let imageId;
    let albumId;

    // Adding listeners
    addCommentForm.addEventListener('submit', function(event) {
        event.preventDefault();
        addComment();
    });

    deleteImageForm.addEventListener('submit', function(event) {
        event.preventDefault();
        deleteImage();
    });

    // Function to open the Image page
    this.open = function(albumIdFromAlbumPage, imageIdFromAlbumPage) {
        imageId = imageIdFromAlbumPage;
        albumId = albumIdFromAlbumPage;

        // Construct query string
        var url = "GoToImagePage?imageId=" + encodeURIComponent(imageId);

        makeCall("GET", url, null, function(x) {
            if (x.readyState === XMLHttpRequest.DONE) {
                var message = x.responseText;
                switch (x.status) {
                    case 200:
                        var jsonObject = JSON.parse(message);
                        var image = jsonObject.image;
                        var comments = jsonObject.comments;
                        var canBeDeleted = jsonObject.canBeDeleted;

                        setImage(image);
                        setImageInfo(image, canBeDeleted);
                        setComments(comments);

                        self.openModal();
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

        comments.forEach(comment => {
            let p = document.createElement("p");
            p.innerHTML = `<strong>${comment.key}</strong>: <span>${comment.value}</span>`;
            commentsWrapper.appendChild(p);
        });
    }

    function addComment() {
        let formData = new FormData(addCommentForm);

        makeCall("POST", "AddComment", formData, function(x) {
            if (x.readyState === XMLHttpRequest.DONE) {
                var message = x.responseText;
                switch (x.status) {
                    case 200:
                        showSuccessAlert(message);
                        pageOrchestrator.showPage("image", albumId, imageId);
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

    function deleteImage() {
        let formData = new FormData(deleteImageForm);

        makeCall("POST", "DeleteImage", formData, function(x) {
            if (x.readyState === XMLHttpRequest.DONE) {
                var message = x.responseText;
                switch (x.status) {
                    case 200:
                        showSuccessAlert(message);
                        pageOrchestrator.showPage("album", albumId);
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
    };

    // Function to hide the page
    this.hide = function() {
        imagePageDiv.style.display = "none";
    };
}
