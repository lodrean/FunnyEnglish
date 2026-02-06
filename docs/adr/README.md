# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records for the FunnyEnglish project.

## What is an ADR?

An Architecture Decision Record (ADR) captures an important architectural decision made along with its context and consequences. ADRs help teams:

- Remember why decisions were made
- Onboard new team members faster
- Avoid revisiting the same decisions repeatedly
- Maintain alignment across the team

## ADR Format

Each ADR follows a standard format:

```markdown
# ADR-XXX: Title

## Status
Proposed / Accepted / Deprecated / Superseded

## Context
Problem statement and forces at play

## Decision
What was decided

## Consequences
Positive and negative outcomes

## Alternatives Considered
Other options and why they weren't chosen

## References
Links and resources
```

## ADR Index

| Number | Title | Status | Date |
|--------|-------|--------|------|
| ADR-001 | [Example Decision](adr-001-example.md) | Accepted | 2024-01-15 |

## Creating a New ADR

1. Copy `template.md` to `adr-XXX-short-title.md`
2. Fill in all sections
3. Start with status "Proposed"
4. Submit for review
5. Update status to "Accepted" after approval

## Status Definitions

- **Proposed**: Under discussion, not yet decided
- **Accepted**: Approved and being implemented
- **Deprecated**: No longer relevant but kept for history
- **Superseded**: Replaced by a newer ADR (link to new ADR)
