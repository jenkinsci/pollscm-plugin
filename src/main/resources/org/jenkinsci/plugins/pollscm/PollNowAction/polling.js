window.pollAction = function(element, event) {
    event.preventDefault();

    fetch(element.href, {
        method: "post",
        headers: crumb.wrap({})
    });

    hoverNotification('Poll scheduled', element.parentNode);
    return false;
};
