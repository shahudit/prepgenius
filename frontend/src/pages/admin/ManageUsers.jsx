import React, { useEffect, useState } from 'react'
import { Card, Badge, Button, Input, Modal, EmptyState } from '../../components/ui.jsx'
import { formatDate } from '../../utils/formatDate.js'
import { Users, Trash2, Search } from 'lucide-react'
import { getAdminUsers, deleteUserById } from '../../services/adminService.js'

export default function ManageUsers() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [target, setTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  const loadUsers = async () => {
    setLoading(true)
    setError('')

    try {
      const res = await getAdminUsers()

      const registeredUsers = (res?.data?.content || []).filter(
          (user) => user?.role === 'USER'
      )

      setUsers(registeredUsers)
    } catch (err) {
      console.error('Failed to load users:', err)
      setError(
          err?.response?.data?.message ||
          'Failed to load users.'
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadUsers()
  }, [])

  const normalizedQuery = query.trim().toLowerCase()

  const learners = users.filter((user) => {
    const name = user?.name?.toLowerCase() || ''
    const email = user?.email?.toLowerCase() || ''

    return (
        name.includes(normalizedQuery) ||
        email.includes(normalizedQuery)
    )
  })

  const confirmDelete = async () => {
    if (!target?.id) {
      return
    }

    const toDelete = target

    setDeleting(true)
    setError('')

    try {
      await deleteUserById(toDelete.id)

      setTarget(null)

      await loadUsers()
    } catch (err) {
      console.error('Failed to delete user:', err)
      setError(
          err?.response?.data?.message ||
          'Failed to delete user.'
      )
    } finally {
      setDeleting(false)
    }
  }

  return (
      <div className="space-y-6">
        <div className="flex items-center justify-between flex-wrap gap-4">
          <div>
            <p className="text-sm text-teal-600 font-medium mb-1">
              Admin console
            </p>

            <h1 className="font-display text-2xl sm:text-3xl font-bold">
              Manage users
            </h1>
          </div>

          <div className="w-64">
            <div className="relative">
              <Search
                  size={16}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-ink/30"
              />

              <Input
                  placeholder="Search by name or email"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  className="pl-9"
              />
            </div>
          </div>
        </div>

        {error && (
            <div className="p-4 text-sm text-red-600 bg-red-100 border border-red-200 rounded-lg">
              {error}
            </div>
        )}

        {loading ? (
            <Card className="p-6">
              <p className="text-sm text-ink/50">
                Loading users...
              </p>
            </Card>
        ) : learners.length === 0 ? (
            <Card>
              <EmptyState
                  icon={Users}
                  title="No users found"
                  description="No registered learners match your search."
              />
            </Card>
        ) : (
            <Card className="overflow-x-auto">
              <table className="w-full text-sm min-w-[600px]">
                <thead className="bg-surface text-ink/50 text-xs">
                <tr>
                  <th className="text-left font-medium px-5 py-3">
                    Name
                  </th>

                  <th className="text-left font-medium px-5 py-3">
                    Email
                  </th>

                  <th className="text-left font-medium px-5 py-3">
                    Interview Preps
                  </th>

                  <th className="text-left font-medium px-5 py-3">
                    Joined
                  </th>

                  <th className="text-right font-medium px-5 py-3">
                    Action
                  </th>
                </tr>
                </thead>

                <tbody>
                {learners.map((user) => (
                    <tr
                        key={user.id}
                        className="border-t border-line"
                    >
                      <td className="px-5 py-3 font-medium">
                        {user.name}
                      </td>

                      <td className="px-5 py-3 text-ink/60">
                        {user.email}
                      </td>

                      <td className="px-5 py-3">
                        <Badge tone="neutral">
                          {user.interviewCount ?? 0}
                        </Badge>
                      </td>

                      <td className="px-5 py-3 text-ink/40">
                        {formatDate(user.createdAt)}
                      </td>

                      <td className="px-5 py-3 text-right">
                        <button
                            onClick={() => setTarget(user)}
                            className="p-2 text-ink/30 hover:text-coral-500 transition-colors"
                            aria-label="Delete user"
                        >
                          <Trash2 size={16} />
                        </button>
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
            </Card>
        )}

        <Modal
            open={!!target}
            onClose={() => setTarget(null)}
            title="Delete user"
            footer={
              <>
                <Button
                    variant="outline"
                    onClick={() => setTarget(null)}
                >
                  Cancel
                </Button>

                <Button
                    variant="danger"
                    onClick={confirmDelete}
                    disabled={deleting}
                >
                  {deleting ? 'Deleting...' : 'Delete user'}
                </Button>
              </>
            }
        >
          <p className="text-sm text-ink/60">
            Are you sure you want to delete{' '}
            <strong>{target?.name}</strong>? This will not remove
            their past interview prep records from analytics, but
            they will no longer be able to log in.
          </p>
        </Modal>
      </div>
  )
}