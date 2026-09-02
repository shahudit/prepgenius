import React, { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext.jsx'
import { Card, Input, Button } from '../../components/ui.jsx'
import { formatDate } from '../../utils/formatDate.js'
import { UserCircle, KeyRound } from 'lucide-react'
import { getProgress } from '../../services/interviewService.js'

export default function Profile() {
  const { currentUser, updateProfile, changePassword } = useAuth()
  const [interviewCount, setInterviewCount] = useState(null)

  const [name, setName] = useState(currentUser.name)
  const [email, setEmail] = useState(currentUser.email)
  const [savingProfile, setSavingProfile] = useState(false)
  const [profileMessage, setProfileMessage] = useState(null)

  const [pwForm, setPwForm] = useState({ old: '', next: '', confirm: '' })
  const [savingPassword, setSavingPassword] = useState(false)
  const [passwordMessage, setPasswordMessage] = useState(null)

  useEffect(() => {
    const loadProgress = async () => {
      try {
        const res = await getProgress()
        setInterviewCount(res?.data?.totalInterviews ?? 0)
      } catch (err) {
        console.error('Failed to load progress:', err)
      }
    }
    loadProgress()
  }, [])

  const saveProfile = async (e) => {
    e.preventDefault()
    setSavingProfile(true)
    setProfileMessage(null)
    const result = await updateProfile(name, email)
    if (result.ok) {
      setProfileMessage({ tone: 'success', text: 'Profile updated.' })
    } else {
      setProfileMessage({ tone: 'error', text: result.error })
    }
    setSavingProfile(false)
  }

  const submitPassword = async (e) => {
    e.preventDefault()
    setPasswordMessage(null)

    if (pwForm.next !== pwForm.confirm) {
      setPasswordMessage({ tone: 'error', text: 'New password and confirmation do not match.' })
      return
    }

    setSavingPassword(true)
    const result = await changePassword(pwForm.old, pwForm.next)
    if (result.ok) {
      setPasswordMessage({ tone: 'success', text: 'Password updated.' })
      setPwForm({ old: '', next: '', confirm: '' })
    } else {
      setPasswordMessage({ tone: 'error', text: result.error })
    }
    setSavingPassword(false)
  }

  return (
    <div className="max-w-2xl space-y-6">
      <div>
        <p className="text-sm text-primary-600 font-medium mb-1">Account</p>
        <h1 className="font-display text-2xl sm:text-3xl font-bold">My profile</h1>
      </div>

      <Card className="p-6">
        <div className="flex items-center gap-3 mb-6">
          <div className="w-12 h-12 rounded-full bg-primary-50 flex items-center justify-center">
            <UserCircle size={26} className="text-primary-500" />
          </div>
          <div>
            <p className="font-medium">{currentUser.name}</p>
            <p className="text-xs text-ink/40">
              Member since {formatDate(currentUser.createdAt)}
              {interviewCount !== null ? ` · ${interviewCount} interview prep${interviewCount !== 1 ? 's' : ''} taken` : ''}
            </p>
          </div>
        </div>

        <form onSubmit={saveProfile} className="space-y-4">
          <Input label="Full name" value={name} onChange={(e) => setName(e.target.value)} required />
          <Input label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          {profileMessage && (
            <p className={`text-sm ${profileMessage.tone === 'success' ? 'text-teal-600' : 'text-coral-500'}`}>
              {profileMessage.text}
            </p>
          )}
          <div className="flex items-center gap-3">
            <Button type="submit" disabled={savingProfile}>{savingProfile ? 'Saving...' : 'Save changes'}</Button>
          </div>
        </form>
      </Card>

      <Card className="p-6">
        <div className="flex items-center gap-2 mb-5">
          <KeyRound size={18} className="text-primary-500" />
          <p className="font-display font-semibold">Change password</p>
        </div>
        <form onSubmit={submitPassword} className="space-y-4">
          <Input
            label="Current password"
            type="password"
            value={pwForm.old}
            onChange={(e) => setPwForm({ ...pwForm, old: e.target.value })}
            required
          />
          <Input
            label="New password"
            type="password"
            value={pwForm.next}
            onChange={(e) => setPwForm({ ...pwForm, next: e.target.value })}
            required
          />
          <Input
            label="Confirm new password"
            type="password"
            value={pwForm.confirm}
            onChange={(e) => setPwForm({ ...pwForm, confirm: e.target.value })}
            required
          />
          {passwordMessage && (
            <p className={`text-sm ${passwordMessage.tone === 'success' ? 'text-teal-600' : 'text-coral-500'}`}>
              {passwordMessage.text}
            </p>
          )}
          <div className="flex items-center gap-3">
            <Button type="submit" variant="secondary" disabled={savingPassword}>{savingPassword ? 'Updating...' : 'Update password'}</Button>
          </div>
        </form>
      </Card>
    </div>
  )
}
