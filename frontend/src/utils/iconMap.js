import {
  Braces, Network, LayoutTemplate, Server, Database, Users, Layers,
  Code2, Cloud, Cpu, Terminal, GitBranch, Brain, Shuffle
} from 'lucide-react'

const map = {
  Braces, Network, LayoutTemplate, Server, Database, Users, Layers,
  Code2, Cloud, Cpu, Terminal, GitBranch, Brain, Shuffle
}

export function getIcon(name) {
  return map[name] || Layers
}
