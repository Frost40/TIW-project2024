/**
 * Loading Div - Loading interface
 * -----------------------------------------------------------------------------------------------------------
 * Description: If the class "loading" is added to body, a modal waiting window is shown. 
 *              To hide the window, just remove class "loading" from body.
 * 
 * Notes:
 * - Object instance is global on purpose as to be used if needed in other js files.
 * - This object is a shared resource, ensuring that only a loading message is open at a time
 * - It's even possible to import css and append message div directly to the document
 *   in an init event. In this case, was chose not to do so, to optimize the loading of the page.
 * - This object does not conflict with eventual other body's classes
 * 
 * @param {*} loading_msg Alternative loading message to load
 */
function LoadingModal(loading_msg) {
	
    this.loading_msg = loading_msg;
    
    this.show = function(message) {
        
        this.update(message);
        
        if (!document.body.className.includes("loading")) {
        
            document.body.className += " loading";
    	}
    };
    
    this.update = function(message) {
		// If a message is supplied to method
        if (message) {
        
            this.loading_msg.textContent = message;
        
        } else {
	
            this.loading_msg.textContent = "Communicating with Server ...";
        }  
    };
    
    this.hide = function() {
	
        document.body.className = document.body.className.replace(" loading", "");
    };
}

//Creates and init object
var loadingModal = new LoadingModal(document.getElementById("loading_msg"));

/**
 * Implicit submission for forms - ENTER key submission
 * -----------------------------------------------------------------------------------------------------------
 * Description: Whenever the user press ENTER on an input of a form, the first button 
 *              found in the form is clicked.
 * Notes:
 * - Hidden inputs are obviously excluded.
 * - When input is single-line text, default action is performing a submit. To override this behaviour,
 *   we prevent default action.
 *   (see https://www.w3.org/MarkUp/html-spec/html-spec_8.html#SEC8.2)
 */
(function (){
	
    var forms = document.getElementsByTagName("form");
    
    Array.from(forms).forEach(form => {
	
        var input_fields = form.querySelectorAll('input:not([type="button"]):not([type="hidden"])');
        var button       = form.querySelector('input[type="button"]');
        
        Array.from(input_fields).forEach(input => {
	
            input.addEventListener("keydown", (e) => {
	
                if(e.keyCode == 13) {
	
                    e.preventDefault();
                    let click = new Event("click");
                    button.dispatchEvent(click);
                }
            });
        });
    });
})();

/**
 * AJAX call management
 * -----------------------------------------------------------------------------------------------------------
 * Description: Make a call to server using AJAX, and report the results.
 * 
 * @param {*} method GET/POST method
 * @param {*} relativeUrl relative url of the call   
 * @param {*} form form to serialize and send. If null, empty request is sent.
 * @param {*} done_callback callback when the request is completed. Takes as argument an XMLHttpRequest
 * @param {*} reset Optionally reset the form (if provided). Default true
 */
function makeCall(method, url, formElement, cback) {
    let req = new XMLHttpRequest(); // visible by closure
    req.onreadystatechange = function () {
        cback(req);
    }; // closure
    req.open(method, url);
    if (formElement !== null) {
        var formData = new FormData(formElement)
        req.send(formData)
    } else {
        req.send();
    }
}

/**
 * Redirect check
 * -----------------------------------------------------------------------------------------------------------
 * Description: Checks if an AJAX call has been redirected. 
 *              This means that auth is no longer valid.
 *
 * Notes:
 * - It's not possible to detect a redirect using response status code, as
 *   if a request is made to the same origin, or the server has CORS enabled,
 *   the 3XX response is followed transparently by XMLHttpRequest.
 *   This default behaviour is not overridable.
 *   (see https://xhr.spec.whatwg.org/#dom-xmlhttprequest-readystate)
 * 
 * - The value of req.responseURL will be the final URL obtained after any redirects.
 *   (see https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/responseURL)
 * 
 * - As pointed out here https://stackoverflow.com/questions/8056277/how-to-get-response-url-in-xmlhttprequest,
 *   req.responseURL could be empty when CORS request is blocked, or redirection loop is detected.
 *
 * @param {*} requestURL Request relative url of the call
 * @param {*} responseURL Response url after eventual redirects
 */
function checkRedirect(requestURL, responseURL){
	
    if (responseURL) {
	
        let actualRequestURL = relPathToAbs(requestURL);
        // If the Url changed
        if (actualRequestURL != responseURL){ 
        	// Navigates to the url
            window.location.assign(responseURL); 
            return false;
        }
        
        // Passes the request to callback
        return true; 
    }
    // Else is CORS blocked or redirection loop 
    console.error("Invalid AJAX call");
    return false;
}

/**
 * Relative/Absolute path adapter
 * -----------------------------------------------------------------------------------------------------------
 * Description: Returns absolute path from relative
 *              (see https://stackoverflow.com/questions/14780350/convert-relative-path-to-absolute-using-javascript)
 * 
 * @param {*} relative Relative path for the request
 */
function relPathToAbs(relative) {
	
    var stack = window.location.href.split("/"),
        parts = relative.split("/");
    // Removes current file name (or empty string)
    stack.pop(); 
    
    for (var i=0; i<parts.length; i++) {
	
        if (parts[i] == ".") {
            
            continue;
        }
        
        if (parts[i] == "..") {
			// Moves one directory back (or up)
            stack.pop(); 
        
        } else {
			// Adds it to path
            stack.push(parts[i]); 
        }
    }
    // Joins everything
    return stack.join("/"); 
}

/**
 * Array.contains re-definition
 * -----------------------------------------------------------------------------------------------------------
 * Description: Adding a modified version of standard include,
 *              with automatic cast during comparison.
 */
Array.prototype.contains = function(element){ 
	
    for(let i = 0;i<this.length;i++) {
    
        if (this[i] == element) {
            
            return true;
        }
    }
    
    return false;
}

/**
 * isNaturalNumber function
 * -----------------------------------------------------------------------------------------------------------
 * Description: Verifies if the given number as input is a natural number or not. Returns true if so
 *
 * @param {*} n variable to check if it's a natural number or not
 */
 function isNaturalNumber(n) {
	// force the value incase it is not
    n = n.toString(); 
    var n1 = Math.abs(n),
        n2 = parseInt(n, 10);
    return !isNaN(n1) && n2 === n1 && n1.toString() === n;
}

function showErrorAlert(message) {
  Swal.fire({
    title: 'Error!',
    text: message,
    icon: 'error',
    confirmButtonText: 'OK'
  });
}

function showSuccessAlert(message) {
  Swal.fire({
    title: 'Success!',
    text: message,
    icon: 'success',
    confirmButtonText: 'OK'
  });
}

function showInfoAlert(message) {
  Swal.fire({
    title: 'Not authorized!',
    text: message,
    icon: 'info',
    confirmButtonText: 'OK'
  });
}