# Claude Code Configuration

This directory contains configuration and custom commands for Claude Code.

## Directory Structure

```
.claude/
├── README.md              # This file
├── agents/                # Subagent definitions
│   ├── analyst.md         # Requirements analysis
│   ├── architect.md       # Architecture design
│   ├── developer.md       # Code implementation
│   ├── reviewer.md        # Code review
│   ├── qa.md              # Quality assurance
│   ├── tech-writer.md     # Documentation
│   └── validator.md       # Quality gate validation
├── commands/              # Slash commands
│   ├── plan.md            # Create implementation plan
│   ├── research.md        # Research codebase
│   ├── implement.md       # Implement from tasklist
│   ├── review.md          # Code review
│   ├── qa.md              # Run QA checks
│   ├── techdebt.md        # Find technical debt
│   ├── prove.md           # Verify changes
│   ├── elegant.md         # Elegant refactoring
│   ├── quiz.md            # Understanding check
│   └── explain-visual.md  # Visual explanation
└── hooks/                 # CI/CD hooks
    ├── pre-commit.sh      # Pre-commit checks
    ├── validate-gates.sh  # Validate quality gates
    └── README.md          # Hooks documentation
```

## Quick Start

### Using Slash Commands

Type `/` followed by command name:

```
/plan my-feature
/research authentication
/implement auth-refresh
/review auth-refresh
/qa auth-refresh
```

### Using Subagents

Spawn specialized agents for specific tasks:

```python
# Analyze requirements
Task(subagent_name="analyst", 
     description="Analyze feature requirements",
     prompt="Analyze the requirements for adding user authentication...")

# Design architecture
Task(subagent_name="architect",
     description="Design authentication architecture", 
     prompt="Design the architecture for JWT-based authentication...")

# Implement code
Task(subagent_name="developer",
     description="Implement auth service",
     prompt="Implement the authentication service following the plan...")

# Review code
Task(subagent_name="reviewer",
     description="Review auth implementation",
     prompt="Review the authentication implementation for...")

# Test implementation
Task(subagent_name="qa",
     description="Test auth feature",
     prompt="Test the authentication feature and create QA report...")
```

## AIDD Workflow

The AI-Driven Development workflow follows these stages:

```
Idea → Research → Plan → PRD → Tasklist → Implement → Review → QA → Docs
 │       │        │     │        │          │         │      │     │
 ▼       ▼        ▼     ▼        ▼          ▼         ▼      ▼     ▼
GATE 1  GATE 2  GATE 3 GATE 4  GATE 5     GATE 6   GATE 7  GATE 8 GATE 9
```

Each stage has specific outputs and quality gates.

## Agents

### Analyst
- **Input**: Feature idea or request
- **Output**: `docs/research/<ticket>.md`
- **When to use**: Understanding requirements, exploring codebase

### Architect
- **Input**: Research findings
- **Output**: `docs/plan/<ticket>.md`, `docs/adr/*.md`
- **When to use**: System design, technology decisions

### Developer
- **Input**: Approved plan and tasklist
- **Output**: Code changes
- **When to use**: Writing implementation code

### Reviewer
- **Input**: Code changes
- **Output**: Review comments
- **When to use**: Code review, quality checks

### QA
- **Input**: Implemented code
- **Output**: `reports/qa/<ticket>.md`
- **When to use**: Testing, verification

### Tech Writer
- **Input**: Completed feature
- **Output**: Updated documentation
- **When to use**: Documentation updates

### Validator
- **Input**: All artifacts
- **Output**: Validation report
- **When to use**: Pre-release verification

## Document Templates

See parent project documentation:
- `docs/research/` - Research findings
- `docs/plan/` - Implementation plans
- `docs/prd/` - Product requirements
- `docs/tasklist/` - Task lists
- `docs/adr/` - Architecture decisions
- `reports/qa/` - QA reports

## Configuration

### settings.local.json
Local Claude Code settings (not committed to git):
```json
{
  "model": "claude-3-opus",
  "temperature": 0.7
}
```

### Environment Variables
Claude Code respects these environment variables:
- `CLAUDE_CODE_DEBUG` - Enable debug output
- `CLAUDE_CODE_MODEL` - Default model to use

## Best Practices

1. **Use plan mode** for non-trivial tasks
2. **Spawn subagents** to keep context clean
3. **Follow quality gates** - don't skip stages
4. **Update tasklists** as you complete tasks
5. **Commit incrementally** after each task
6. **Run validation** before claiming complete

## Troubleshooting

### Command not found
Ensure command files end with `.md` and have proper frontmatter.

### Agent not responding
Check agent file has valid YAML frontmatter with name and description.

### Hook not running
Ensure hook scripts are executable: `chmod +x .claude/hooks/*.sh`

## References

- [Claude Code Documentation](https://docs.anthropic.com/en/docs/agents-and-tools/claude-code/overview)
- [AIDD Article (Habr)](https://habr.com/ru/articles/974924/)
- Project `CLAUDE.md` for project-specific instructions
