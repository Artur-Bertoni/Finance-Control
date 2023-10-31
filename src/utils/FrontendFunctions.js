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

    let li = document.createElement('li')
    li.appendChild(deleteButton)

    let iconButtonList = document.getElementById('navigation-menu-btn-gp')
    iconButtonList.appendChild(li)
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
    homePageButton.type = 'submit'
    homePageButton.appendChild(homePageImg)

    let li = document.createElement('li')
    li.appendChild(homePageButton)

    let iconButtonList = document.getElementById('navigation-menu-btn-gp')
    iconButtonList.appendChild(li)
}

export function doRequest(url, method, ...extraParams) {
    let data = {...method, ...Object.assign({}, ...extraParams)};

    $.ajax({
        url: url,
        type: 'POST',
        async: false,
        data: data,
        success: function (response) {
            data = JSON.parse(response);
        },
        error: function (error) {
            console.error(error);
        }
    });

    return data;
}