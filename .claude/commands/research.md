---
name: research
description: Research codebase for a ticket and create research document
---

# /research Command

Research the codebase to understand implementation requirements for a ticket.

## Usage
```
/research <ticket-name>
```

## Prerequisites
- Ticket ID is defined
- Initial idea/request is documented

## Process

1. Read any existing context about the ticket
2. Search codebase for relevant files
3. Identify affected areas
4. Find existing patterns
5. Assess complexity
6. Create `docs/research/<ticket-name>.md`

## Output

Research document containing:
- Objective and scope
- Affected files and areas
- Existing implementation patterns
- Complexity assessment (Low/Medium/High)
- Open questions to resolve
- Recommendations

## Example

```
User: /research push-notifications

Claude:
I'll research the codebase for implementing push notifications.

Research findings:
- Backend: Need FCM integration in NotificationService
- Mobile: WorkManager already configured, need to add FCM
- Shared: Add notification models
- Complexity: HIGH (requires Firebase setup, APNs for iOS)

Created: docs/research/push-notifications.md
```
