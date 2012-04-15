function(data) {
    // Set field in app so that everyone can access the site_id
    var app = $$(this).app;
    
    //$(this).trigger("post_changes");
    
    $.log("received data: " + JSON.stringify(data));
    
    // Set title for the document
    document.title = "Classement2";
    
    return;
};
