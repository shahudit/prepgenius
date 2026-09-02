import React, { useEffect, useState } from 'react'
import { Card, Button, Input, Select, Modal, EmptyState, Badge } from '../../components/ui.jsx'
import { getIcon } from '../../utils/iconMap.js'
import { Layers, Plus, Pencil, Trash2 } from 'lucide-react'
import {
  getAdminCategories,
  createCategory,
  updateCategoryById,
  deleteCategoryById,
} from '../../services/adminService.js'

const groupOptions = [
  { id: 'TECHNICAL', label: 'Technical (topic/domain)' },
  { id: 'HR', label: 'HR' },
  { id: 'APTITUDE', label: 'Aptitude' },
  { id: 'MIXED', label: 'Mixed' },
]
const groupTone = { TECHNICAL: 'primary', HR: 'coral', APTITUDE: 'teal', MIXED: 'neutral' }
const groupIcon = { TECHNICAL: 'Code2', HR: 'Users', APTITUDE: 'Brain', MIXED: 'Shuffle' }
const emptyForm = { name: '', description: '', group: 'TECHNICAL' }

export default function ManageCategories() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [saving, setSaving] = useState(false)

  const loadCategories = async () => {
    setLoading(true)
    setError('')
    try {
      const res = await getAdminCategories()
      setCategories(res?.data?.content || [])
    } catch (err) {
      console.error('Failed to load categories:', err)
      setError(err?.response?.data?.message || 'Failed to load categories.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadCategories()
  }, [])

  const openAdd = () => {
    setEditing(null)
    setForm(emptyForm)
    setModalOpen(true)
  }
  const openEdit = (c) => {
    setEditing(c)
    setForm({ name: c.name, description: c.description || '', group: c.group || 'TECHNICAL' })
    setModalOpen(true)
  }
  const save = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      if (editing) {
        await updateCategoryById(editing.id, { ...form, active: true })
      } else {
        await createCategory({ ...form, active: true })
      }
      setModalOpen(false)
      await loadCategories()
    } catch (err) {
      console.error('Failed to save category:', err)
      setError(err?.response?.data?.message || 'Failed to save category.')
    } finally {
      setSaving(false)
    }
  }

  const confirmDelete = async () => {
    const target = deleteTarget
    setDeleteTarget(null)
    try {
      await deleteCategoryById(target.id)
      await loadCategories()
    } catch (err) {
      console.error('Failed to delete category:', err)
      setError(err?.response?.data?.message || 'Failed to delete category.')
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <p className="text-sm text-teal-600 font-medium mb-1">Admin console</p>
          <h1 className="font-display text-2xl sm:text-3xl font-bold">Manage categories</h1>
        </div>
        <Button onClick={openAdd}><Plus size={16} /> Add category</Button>
      </div>

      {error && (
        <div className="p-4 text-sm text-red-600 bg-red-100 border border-red-200 rounded-lg">
          {error}
        </div>
      )}

      {loading ? (
        <Card className="p-6"><p className="text-sm text-ink/50">Loading categories...</p></Card>
      ) : categories.length === 0 ? (
        <Card><EmptyState icon={Layers} title="No categories yet" action={<Button onClick={openAdd}>Add your first category</Button>} /></Card>
      ) : (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {categories.map((c) => {
            const Icon = getIcon(groupIcon[c.group] || 'Layers')
            return (
              <Card key={c.id} className="p-5">
                <div className="flex items-start justify-between mb-2">
                  <div className="w-10 h-10 rounded-lg bg-primary-50 flex items-center justify-center">
                    <Icon size={18} className="text-primary-500" />
                  </div>
                  <div className="flex gap-1">
                    <button onClick={() => openEdit(c)} className="p-1.5 text-ink/30 hover:text-primary-600"><Pencil size={15} /></button>
                    <button onClick={() => setDeleteTarget(c)} className="p-1.5 text-ink/30 hover:text-coral-500"><Trash2 size={15} /></button>
                  </div>
                </div>
                <p className="font-medium text-sm">{c.name}</p>
                <div className="flex items-center gap-2 mt-1.5">
                  <Badge tone={groupTone[c.group] || 'neutral'}>
                    {groupOptions.find((g) => g.id === c.group)?.label.split(' ')[0] || 'Technical'}
                  </Badge>
                  {c.description && (
                    <p className="text-xs text-ink/45">{c.description}</p>
                  )}
                </div>
              </Card>
            )
          })}
        </div>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editing ? 'Edit category' : 'Add category'}
        footer={
          <>
            <Button variant="outline" onClick={() => setModalOpen(false)}>Cancel</Button>
            <Button onClick={save} disabled={saving}>{saving ? 'Saving...' : editing ? 'Save changes' : 'Add category'}</Button>
          </>
        }
      >
        <form onSubmit={save} className="space-y-4">
          <Input label="Category name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <Select label="Type" value={form.group} onChange={(e) => setForm({ ...form, group: e.target.value })}>
            {groupOptions.map((g) => <option key={g.id} value={g.id}>{g.label}</option>)}
          </Select>
          <Input label="Description (optional)" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="e.g. Core Java, OOP, Collections" />
        </form>
      </Modal>

      <Modal
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        title="Delete category"
        footer={
          <>
            <Button variant="outline" onClick={() => setDeleteTarget(null)}>Cancel</Button>
            <Button variant="danger" onClick={confirmDelete}>Delete</Button>
          </>
        }
      >
        <p className="text-sm text-ink/60">Delete <strong>{deleteTarget?.name}</strong>? Existing questions in this category will remain but become unreachable from setup.</p>
      </Modal>
    </div>
  )
}
