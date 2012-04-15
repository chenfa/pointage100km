function(data) {
    // Set field in app so that everyone can access the site_id
    var app = $$(this).app;
    
    $(this).trigger("ranking_title_div_finish_load");
    
    $.log("received data: " + JSON.stringify(data));
    
    app.contestants = data;

    // Set title for the document
    document.title = "Classement2";
    
    return;
};
