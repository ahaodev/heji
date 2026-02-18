import { ContentSection } from '../components/content-section'
import { AccountForm } from './account-form'

export function SettingsAccount() {
  return (
    <ContentSection
      title='账户设置'
      desc='更新您的账户设置。设置您喜欢的语言和时区。'
    >
      <AccountForm />
    </ContentSection>
  )
}
