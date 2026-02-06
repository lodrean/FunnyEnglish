#!/bin/bash
# Validate all quality gates for a ticket
# Usage: ./validate-gates.sh <ticket-id>

TICKET=$1

if [ -z "$TICKET" ]; then
    echo "Usage: $0 <ticket-id>"
    exit 1
fi

echo "🔍 Validating quality gates for: $TICKET"
echo ""

PASSED=0
FAILED=0

# Gate 2: RESEARCH_COMPLETE
echo "📋 Gate 2: RESEARCH_COMPLETE"
if [ -f "docs/research/$TICKET.md" ]; then
    echo "   ✅ Research document exists"
    PASSED=$((PASSED + 1))
else
    echo "   ❌ Missing: docs/research/$TICKET.md"
    FAILED=$((FAILED + 1))
fi

# Gate 3: PLAN_APPROVED
echo "📋 Gate 3: PLAN_APPROVED"
if [ -f "docs/plan/$TICKET.md" ]; then
    echo "   ✅ Plan document exists"
    if grep -q "## Status" "docs/plan/$TICKET.md" && grep -A1 "## Status" "docs/plan/$TICKET.md" | grep -q "APPROVED"; then
        echo "   ✅ Status is APPROVED"
        PASSED=$((PASSED + 1))
    else
        echo "   ⚠️  Status not APPROVED"
        FAILED=$((FAILED + 1))
    fi
else
    echo "   ❌ Missing: docs/plan/$TICKET.md"
    FAILED=$((FAILED + 1))
fi

# Gate 4: PRD_READY
echo "📋 Gate 4: PRD_READY"
if [ -f "docs/prd/$TICKET.prd.md" ]; then
    echo "   ✅ PRD document exists"
    if grep -q "## Status" "docs/prd/$TICKET.prd.md" && grep -A1 "## Status" "docs/prd/$TICKET.prd.md" | grep -q "READY"; then
        echo "   ✅ Status is READY"
        PASSED=$((PASSED + 1))
    else
        echo "   ⚠️  Status not READY"
        FAILED=$((FAILED + 1))
    fi
else
    echo "   ❌ Missing: docs/prd/$TICKET.prd.md"
    FAILED=$((FAILED + 1))
fi

# Gate 5: TASKLIST_READY
echo "📋 Gate 5: TASKLIST_READY"
if [ -f "docs/tasklist/$TICKET.md" ]; then
    echo "   ✅ Tasklist exists"
    # Check for incomplete tasks
    INCOMPLETE=$(grep -c "^\- \[ \]" "docs/tasklist/$TICKET.md" 2>/dev/null || echo "0")
    if [ "$INCOMPLETE" -eq 0 ]; then
        echo "   ✅ All tasks complete"
        PASSED=$((PASSED + 1))
    else
        echo "   ⚠️  $INCOMPLETE incomplete tasks"
        FAILED=$((FAILED + 1))
    fi
else
    echo "   ❌ Missing: docs/tasklist/$TICKET.md"
    FAILED=$((FAILED + 1))
fi

# Gate 8: QA_PASS
echo "📋 Gate 8: QA_PASS"
if [ -f "reports/qa/$TICKET.md" ]; then
    echo "   ✅ QA report exists"
    if grep -q "## Summary" "reports/qa/$TICKET.md" && grep -A5 "## Summary" "reports/qa/$TICKET.md" | grep -iq "PASS"; then
        echo "   ✅ QA passed"
        PASSED=$((PASSED + 1))
    else
        echo "   ⚠️  QA not passed"
        FAILED=$((FAILED + 1))
    fi
else
    echo "   ❌ Missing: reports/qa/$TICKET.md"
    FAILED=$((FAILED + 1))
fi

echo ""
echo "═══════════════════════════════════════════"
echo "  Quality Gates: $PASSED passed, $FAILED failed"
echo "═══════════════════════════════════════════"

if [ $FAILED -eq 0 ]; then
    echo "🎉 All quality gates passed! Ready for release."
    exit 0
else
    echo "⚠️  Some quality gates failed. Please address before release."
    exit 1
fi
