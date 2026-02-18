import { ContentSection } from '../components/content-section'
import { ProfileForm } from './profile-form'

export function SettingsProfile() {
  return (
    <ContentSection
      title='个人资料'
      desc='编辑个人资料。'
    >
      <ProfileForm />
    </ContentSection>
  )
}
