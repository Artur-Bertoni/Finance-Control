export function addDeleteIcon() {
    let deleteImg = document.createElement('img')
    deleteImg.alt = 'Delete Picture'
    deleteImg.id = 'delete-btn-img'
    deleteImg.src = '../images/delete.png'

    let deleteButton = document.createElement('button')
    deleteButton.classList.add('img-btn')
    deleteButton.id = 'delete-btn'
    deleteButton.name = 'deleteButton'
    deleteButton.type = 'button'
    deleteButton.appendChild(deleteImg)

    let li = document.createElement('li')
    li.appendChild(deleteButton)

    document.getElementById('navigation-menu-btn-gp').appendChild(li)
    return deleteButton
}

export function addHomePageIcon() {
    let homePageImg = document.createElement('img')
    homePageImg.alt = 'Home Page Picture'
    homePageImg.id = 'home-btn-img'
    homePageImg.src = '../images/home-page.png'

    let homePageButton = document.createElement('button')
    homePageButton.classList.add('img-btn')
    homePageButton.id = 'home-btn'
    homePageButton.name = 'homeButton'
    homePageButton.type = 'button'
    homePageButton.appendChild(homePageImg)

    let li = document.createElement('li')
    li.appendChild(homePageButton)

    document.getElementById('navigation-menu-btn-gp').appendChild(li)
    return homePageButton
}

export function navigate(url) {
    window.location.href = url
}

export function doRequest(url, httpMethod = 'GET', body = null) {
    let result = null
    $.ajax({
        url: url,
        type: httpMethod,
        async: false,
        contentType: 'application/json',
        data: body !== null ? JSON.stringify(body) : undefined,
        success: function (response) {
            result = response
        },
        error: function (xhr) {
            console.error('Request error:', xhr.status, xhr.responseText)
        }
    })
    return result
}
