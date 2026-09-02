import React, { useEffect, useState } from 'react'
import { Card, Button, Input, Modal, EmptyState } from '../../components/ui.jsx'
import { Building2, Plus, Pencil, Trash2 } from 'lucide-react'
import {
  getAdminCompanies,
  createCompany,
  updateCompanyById,
  deleteCompanyById,
} from '../../services/adminService.js'

const emptyForm = { name: '', focus: '' }

export default function ManageCompanies() {
  const [companies, setCompanies] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [saving, setSaving] = useState(false)

  const loadCompanies = async () => {
    setLoading(true)
    setError('')
    try {
      const res = await getAdminCompanies()
      setCompanies(res?.data?.content || [])
    } catch (err) {
      console.error('Failed to load companies:', err)
      setError(err?.response?.data?.message || 'Failed to load companies.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadCompanies()
  }, [])

  const openAdd = () => {
    setEditing(null)
    setForm(emptyForm)
    setModalOpen(true)
  }
  const openEdit = (c) => {
    setEditing(c)
    setForm({ name: c.name, focus: c.focus || '' })
    setModalOpen(true)
  }
  const save = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      if (editing) {
        await updateCompanyById(editing.id, { ...form, active: true })
      } else {
        await createCompany({ ...form, active: true })
      }
      setModalOpen(false)
      await loadCompanies()
    } catch (err) {
      console.error('Failed to save company:', err)
      setError(err?.response?.data?.message || 'Failed to save company.')
    } finally {
      setSaving(false)
    }
  }

  const confirmDelete = async () => {
    const target = deleteTarget
    setDeleteTarget(null)
    try {
      await deleteCompanyById(target.id)
      await loadCompanies()
    } catch (err) {
      console.error('Failed to delete company:', err)
      setError(err?.response?.data?.message || 'Failed to delete company.')
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <p className="text-sm text-teal-600 font-medium mb-1">Admin console</p>
          <h1 className="font-display text-2xl sm:text-3xl font-bold">Manage companies</h1>
        </div>
        <Button onClick={openAdd}><Plus size={16} /> Add company</Button>
      </div>

      {error && (
        <div className="p-4 text-sm text-red-600 bg-red-100 border border-red-200 rounded-lg">
          {error}
        </div>
      )}

      {loading ? (
        <Card className="p-6"><p className="text-sm text-ink/50">Loading companies...</p></Card>
      ) : companies.length === 0 ? (
        <Card><EmptyState icon={Building2} title="No companies yet" action={<Button onClick={openAdd}>Add your first company</Button>} /></Card>
      ) : (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {companies.map((c) => (
            <Card key={c.id} className="p-5">
              <div className="flex items-start justify-between mb-2">
                <div className="w-10 h-10 rounded-lg bg-primary-50 flex items-center justify-center">
                  <Building2 size={18} className="text-primary-500" />
                </div>
                <div className="flex gap-1">
                  <button onClick={() => openEdit(c)} className="p-1.5 text-ink/30 hover:text-primary-600"><Pencil size={15} /></button>
                  <button onClick={() => setDeleteTarget(c)} className="p-1.5 text-ink/30 hover:text-coral-500"><Trash2 size={15} /></button>
                </div>
              </div>
              <p className="font-medium text-sm">{c.name}</p>
              <p className="text-xs text-ink/45 mt-1">{c.focus}</p>
            </Card>
          ))}
        </div>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editing ? 'Edit company' : 'Add company'}
        footer={
          <>
            <Button variant="outline" onClick={() => setModalOpen(false)}>Cancel</Button>
            <Button onClick={save} disabled={saving}>{saving ? 'Saving...' : editing ? 'Save changes' : 'Add company'}</Button>
          </>
        }
      >
        <form onSubmit={save} className="space-y-4">
          <Input label="Company name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <Input label="Interview Prep focus areas" value={form.focus} onChange={(e) => setForm({ ...form, focus: e.target.value })} placeholder="e.g. DSA, System Design, Behavioral" />
        </form>
      </Modal>

      <Modal
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        title="Delete company"
        footer={
          <>
            <Button variant="outline" onClick={() => setDeleteTarget(null)}>Cancel</Button>
            <Button variant="danger" onClick={confirmDelete}>Delete</Button>
          </>
        }
      >
        <p className="text-sm text-ink/60">Delete <strong>{deleteTarget?.name}</strong>? This can't be undone.</p>
      </Modal>
    </div>
  )
}
