# Action Buttons Documentation

## Übersicht
Action Buttons erweitern das Action-System von XIS um Button-Elemente, die sich kontextabhängig verhalten.

## Verwendung

### Standalone Buttons
Buttons außerhalb von Formularen verhalten sich wie Action Links:

```html
<div>
    <button xis:action="deleteItem">Löschen</button>
    <button xis:action="showDetails">Details anzeigen</button>
</div>
```

### Buttons mit Parametern
```html
<button xis:action="editItem">
    <xis:parameter name="id" value="${item.id}"/>
    <xis:parameter name="mode" value="edit"/>
    Bearbeiten
</button>
```

### Buttons in Formularen
Buttons innerhalb von Formularen führen Form-Submit mit der angegebenen Action durch:

```html
<form xis:binding="userData">
    <input xis:binding="name" type="text"/>
    <input xis:binding="email" type="email"/>
    <button xis:action="saveUser">Benutzer speichern</button>
</form>
```

## Vergleich: Button vs Link

| Szenario | Button | Link |
|----------|---------|------|
| Außerhalb Form | `<button xis:action="action">` | `<a xis:action="action">` |
| Styling | Button-Styling | Link-Styling |
| Semantik | Aktion/Befehl | Navigation |
| Accessibility | Button-Semantik | Link-Semantik |

## Technische Details

### Automatische type="button" Setzung
- Buttons mit `xis:action` erhalten automatisch `type="button"`
- Verhindert ungewollte Form-Submits
- User muss nicht über HTML-Button-Semantik nachdenken

### Kontextabhängiges Verhalten
- **Außerhalb Form**: Direkte Widget/Page-Action (wie ActionLink)
- **Innerhalb Form**: Form-Submit mit Action (wie Submit-Button)

### Handler-Architektur
- `ActionButtonHandler` für Buttons mit `xis:action`
- `FormSubmitterHandler` für spezielle Submit-Elemente
- Automatische Erkennung im `HandlerBuilder`

## Best Practices

### Wann Button vs Link verwenden?
- **Button**: Für Aktionen, Befehle, Operationen
- **Link**: Für Navigation, Verweise, Downloads

### Beispiele
```html
<!-- Gut: Aktionen als Buttons -->
<button xis:action="save">Speichern</button>
<button xis:action="delete">Löschen</button>
<button xis:action="calculate">Berechnen</button>

<!-- Gut: Navigation als Links -->
<a xis:page="/details.html">Details ansehen</a>
<a xis:widget="DetailWidget">Details laden</a>
```

## Integration mit Reactive State
Action Buttons sind vollständig in das Reactive State System integriert:

```html
<p>Zähler: ${state.counter}</p>
<button xis:action="increment">+1</button>
<button xis:action="decrement">-1</button>
```

Siehe auch: [Reactive State Documentation]