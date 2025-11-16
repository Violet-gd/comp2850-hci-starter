# Testing Notes — Week 7 Lab 1

## HTMX Path
**Date**: [YYYY-MM-DD]
**Browser**: Chrome 120.0
**JavaScript**: Enabled

### Test: Inline edit activation
- **Action**: Clicked "Edit" button
- **Result**: ✅ Form appeared instantly (no page reload)
- **Network**: GET /tasks/1/edit (AJAX, 45ms)
- **Screenshot**: `01-view-mode-htmx.png`, `02-edit-mode-htmx.png`

### Test: Validation error
- **Action**: Deleted title, clicked "Save"
- **Result**: ✅ Error shown: "Title is required..."
- **ARIA**: `<p id="error-1" role="alert" aria-live="assertive">` confirmed in DevTools
- **Screenshot**: `03-validation-error-htmx.png`

### Test: Successful save
- **Action**: Entered valid title, clicked "Save"
- **Result**: ✅ View mode restored, status = "Task 'New title' updated successfully."
- **Screenshot**: `04-status-oob-devtools.png`

---

## No-JS Path
**Date**: [YYYY-MM-DD]
**Browser**: Chrome 120.0
**JavaScript**: Disabled

### Test: Edit activation
- **Action**: Clicked "Edit" button
- **Result**: ✅ Full page reload, form shown
- **URL**: http://localhost:8080/tasks/1/edit

### Test: Validation error
- **Action**: Deleted title, clicked "Save"
- **Result**: ✅ Redirect to /tasks/1/edit?error=blank, error shown
- **Screenshot**: `05-edit-mode-nojs-error.png`

---

## Keyboard Testing
**Date**: [YYYY-MM-DD]
**Input**: Keyboard only (no mouse)

### Test: Tab navigation
- **Path**: Tab → "Edit" → Enter → Title input (autofocus) → Tab → "Save" → Enter
- **Result**: ✅ Focus order logical, all buttons reachable
- **Focus indicators**: ✅ Visible outline on all elements

---

## Screen Reader Testing
**Date**: [YYYY-MM-DD]
**Tool**: NVDA 2024.1 (Windows 11)

### Test: Edit button announcement
- **Navigated to**: "Edit" button
- **NVDA said**: "Edit task: Buy milk, button"
- **Result**: ✅ Contextual label announced

### Test: Input field announcement
- **Activated**: Edit button (Enter)
- **NVDA said**: "Title, edit, Keep it short and specific, Buy milk"
- **Result**: ✅ Label + hint + value announced

### Test: Error announcement
- **Action**: Deleted title, pressed Save
- **NVDA said**: "Title is required. Please enter at least one character, alert"
- **Result**: ✅ Error announced immediately (assertive)

### Test: Status message announcement
- **Action**: Saved valid title
- **NVDA said**: "Task 'New title' updated successfully"
- **Result**: ✅ Status announced (polite, didn't interrupt)

---

## Issues Found
None. All tests passed.

## WCAG Compliance Check
| Criterion | Status | Evidence |
|-----------|--------|----------|
| 2.1.1 Keyboard (A) | ✅ Pass | All features accessible via Tab/Enter |
| 3.3.1 Error Identification (A) | ✅ Pass | Error message explicit, aria-describedby links input |
| 4.1.3 Status Messages (AA) | ✅ Pass | Live region announces success without focus change |
