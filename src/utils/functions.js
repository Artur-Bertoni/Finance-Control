export function addDeleteIcon() {
    let deleteImg = document.createElement('img')
    deleteImg.alt = 'Delete Picture'
    deleteImg.id = 'delete-btn-img'
    deleteImg.src = '../images/delete.png'

    let deleteButton = document.createElement('button')
    deleteButton.classList.add('img-btn')
    deleteButton.id = 'delete-btn'
    deleteButton.name = 'deleteButton'
    deleteButton.type = 'submit'

    deleteButton.appendChild(deleteImg)
    let iconButtonList = document.getElementById('navigation-menu-btn-gp')
    iconButtonList.appendChild(deleteButton)
}

export function doRequest(url, method) {
    let data;

    $.ajax({
        url: url,
        type: 'POST',
        async: false,
        data: method,
        success: function (response) {
            data = JSON.parse(response);
        },
        error: function (error) {
            console.error(error);
        }
    });

    return data;
}