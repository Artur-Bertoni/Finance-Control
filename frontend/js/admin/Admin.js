import { navigate, showToast } from '../../utils/FrontendFunctions.js'
import { SidebarManager } from '../components/SidebarManager.js'
import { MascotManager } from '../components/MascotManager.js'
import { OnboardingTour } from '../components/OnboardingTour.js'
import { I18n } from '../i18n.js'

export async function init() {
    await SidebarManager.initialize()

    if (!globalThis.__currentUser?.admin) {
        navigate('/pages/HomePage.html')
        return
    }

    document.getElementById('admin-feedbacks-btn')?.addEventListener('click',
        () => navigate('/pages/admin/FeedbackAdmin.html'))

    document.getElementById('send-test-email-btn')?.addEventListener('click', sendTestEmail)

    document.getElementById('trigger-onboarding-btn')?.addEventListener('click',
        () => OnboardingTour.start())

    document.getElementById('trigger-tip-btn')?.addEventListener('click',
        () => MascotManager.popTestTip())
}

function sendTestEmail() {
    const type    = document.getElementById('test-email-type').value
    const overlay = document.createElement('div')
    overlay.className = 'loading-overlay'
    overlay.innerHTML = '<div class="loading-spinner"></div>'
    document.body.appendChild(overlay)

    $.ajax({
        url:      `/api/admin/email/send-test?type=${encodeURIComponent(type)}`,
        type:     'POST',
        success:  () => showToast(I18n.t('testEmailSent'), 'success'),
        error:    xhr => showToast(xhr.responseJSON?.message ?? I18n.t('errorSendingTestEmail'), 'error'),
        complete: () => overlay.remove()
    })
}

if (!globalThis.__appRouter) init()
