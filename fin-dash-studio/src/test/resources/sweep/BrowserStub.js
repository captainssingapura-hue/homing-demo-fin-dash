// BrowserStub — the least browser a served widget needs to construct.
//
// Not a browser. A DOM whose elements have children, classes, styles,
// attributes, text and listeners; a document with a head and a body; timers
// that RECORD rather than fire; fetch routed to the desk's real actions
// through a host function; and a console whose errors are kept, because
// "no console errors" is one of the sweep's assertions. Anything a widget
// needs beyond this is added here, once, with the widget that needed it.

globalThis.__mod = {};
globalThis.__console = { errors: [], warns: [] };
globalThis.__timers = { timeouts: [], intervals: [], frames: [] };
globalThis.__listeners = { document: {}, window: {} };

var console = {
    log:   function () {},
    info:  function () {},
    debug: function () {},
    warn:  function () { __console.warns.push(Array.prototype.slice.call(arguments).map(String).join(' ')); },
    error: function () { __console.errors.push(Array.prototype.slice.call(arguments).map(String).join(' ')); }
};

function makeClassList(el) {
    var list = function () { return el._className ? el._className.split(/\s+/).filter(Boolean) : []; };
    var set  = function (arr) { el._className = arr.join(' '); };
    return {
        add:      function () { var a = list(); for (var i = 0; i < arguments.length; i++) if (a.indexOf(arguments[i]) < 0) a.push(arguments[i]); set(a); },
        remove:   function () { var a = list(); for (var i = 0; i < arguments.length; i++) { var k = a.indexOf(arguments[i]); if (k >= 0) a.splice(k, 1); } set(a); },
        contains: function (c) { return list().indexOf(c) >= 0; },
        toggle:   function (c, force) { var has = this.contains(c); var want = force === undefined ? !has : !!force; if (want && !has) this.add(c); if (!want && has) this.remove(c); return want; }
    };
}

function makeEl(tag) {
    var el = {
        nodeType: 1, tagName: String(tag).toUpperCase(), _className: '', _text: '',
        children: [], parentNode: null, _attrs: {}, _listeners: {}, dataset: {},
        style: {
            _props: {},
            setProperty:    function (k, v) { this._props[k] = v; },
            removeProperty: function (k) { delete this._props[k]; },
            getPropertyValue: function (k) { return this._props[k] || ''; }
        },
        value: '', checked: false, disabled: false, selected: false, id: '', title: '',
        appendChild: function (c) {
            if (c.parentNode) c.parentNode.removeChild(c);
            c.parentNode = this; this.children.push(c); return c;
        },
        insertBefore: function (nu, ref) {
            if (nu.parentNode) nu.parentNode.removeChild(nu);
            var i = ref ? this.children.indexOf(ref) : -1;
            if (i < 0) this.children.push(nu); else this.children.splice(i, 0, nu);
            nu.parentNode = this; return nu;
        },
        removeChild: function (c) {
            var i = this.children.indexOf(c);
            if (i >= 0) this.children.splice(i, 1);
            c.parentNode = null; return c;
        },
        replaceChild: function (nu, old) {
            var i = this.children.indexOf(old);
            if (nu.parentNode) nu.parentNode.removeChild(nu);
            this.children[i] = nu; old.parentNode = null; nu.parentNode = this; return old;
        },
        remove: function () { if (this.parentNode) this.parentNode.removeChild(this); },
        contains: function (c) { for (var n = c; n; n = n.parentNode) if (n === this) return true; return false; },
        setAttribute:    function (k, v) { this._attrs[k] = String(v); if (k === 'id') this.id = String(v); },
        getAttribute:    function (k) { return k in this._attrs ? this._attrs[k] : null; },
        removeAttribute: function (k) { delete this._attrs[k]; },
        hasAttribute:    function (k) { return k in this._attrs; },
        addEventListener:    function (t, fn) { (this._listeners[t] = this._listeners[t] || []).push(fn); },
        removeEventListener: function (t, fn) { var a = this._listeners[t] || []; var i = a.indexOf(fn); if (i >= 0) a.splice(i, 1); },
        dispatchEvent: function (ev) {
            ev.target = ev.target || this;
            for (var n = this; n; n = n.parentNode) {
                ev.currentTarget = n;
                var a = (n._listeners && n._listeners[ev.type]) || [];
                for (var i = 0; i < a.length; i++) a[i].call(n, ev);
                if (typeof n['on' + ev.type] === 'function') n['on' + ev.type].call(n, ev);
                if (!ev.bubbles || ev._stopped) break;
            }
            return true;
        },
        click: function () { this.dispatchEvent(new MouseEvent('click', { bubbles: true })); },
        focus: function () {}, blur: function () {}, scrollIntoView: function () {},
        getBoundingClientRect: function () { return { top: 0, left: 0, right: 0, bottom: 0, width: 0, height: 0 }; },
        querySelector:    function () { return null; },
        querySelectorAll: function () { return []; },
        closest: function () { return null; },
        get firstChild()        { return this.children[0] || null; },
        get lastChild()         { return this.children[this.children.length - 1] || null; },
        get firstElementChild() { return this.children[0] || null; },
        get childNodes()        { return this.children; },
        get childElementCount() { return this.children.length; },
        get options()           { return this.children; },
        get offsetWidth()  { return 0; }, get offsetHeight() { return 0; },
        get clientWidth()  { return 0; }, get clientHeight() { return 0; },
        get className()    { return this._className; },
        set className(v)   { this._className = String(v); },
        get textContent()  {
            if (this.children.length === 0) return this._text;
            var s = this._text; for (var i = 0; i < this.children.length; i++) s += this.children[i].textContent; return s;
        },
        set textContent(v) { this.children.forEach(function (c) { c.parentNode = null; }); this.children = []; this._text = String(v); },
        get innerText()    { return this.textContent; },
        set innerText(v)   { this.textContent = v; },
        get innerHTML()    { return this.textContent; },
        set innerHTML(v)   { this.textContent = v; }
    };
    el.classList = makeClassList(el);
    return el;
}

function makeText(s) {
    return { nodeType: 3, tagName: '#text', _text: String(s), children: [], parentNode: null,
             get textContent() { return this._text; }, set textContent(v) { this._text = String(v); } };
}

function Event(type, init) { this.type = type; this.bubbles = !!(init && init.bubbles); this._stopped = false; if (init) for (var k in init) this[k] = init[k]; }
Event.prototype.preventDefault  = function () { this.defaultPrevented = true; };
Event.prototype.stopPropagation = function () { this._stopped = true; };
function MouseEvent(type, init)    { Event.call(this, type, init); }
MouseEvent.prototype = Object.create(Event.prototype);
function KeyboardEvent(type, init) { Event.call(this, type, init); }
KeyboardEvent.prototype = Object.create(Event.prototype);
function CustomEvent(type, init)   { Event.call(this, type, init); this.detail = init && init.detail; }
CustomEvent.prototype = Object.create(Event.prototype);

var document = {
    nodeType: 9,
    head: makeEl('head'), body: makeEl('body'), documentElement: makeEl('html'),
    hidden: false, visibilityState: 'visible',
    createElement:   function (tag) { return makeEl(tag); },
    createElementNS: function (ns, tag) { return makeEl(tag); },
    createTextNode:  function (s) { return makeText(s); },
    createDocumentFragment: function () { return makeEl('#fragment'); },
    addEventListener:    function (t, fn) { (__listeners.document[t] = __listeners.document[t] || []).push(fn); },
    removeEventListener: function (t, fn) { var a = __listeners.document[t] || []; var i = a.indexOf(fn); if (i >= 0) a.splice(i, 1); },
    querySelector:    function () { return null; },
    querySelectorAll: function () { return []; },
    getElementById:   function () { return null; },
    activeElement: null
};
document.documentElement.appendChild(document.head);
document.documentElement.appendChild(document.body);

var window = globalThis;
window.document = document;
window.addEventListener    = function (t, fn) { (__listeners.window[t] = __listeners.window[t] || []).push(fn); };
window.removeEventListener = function (t, fn) { var a = __listeners.window[t] || []; var i = a.indexOf(fn); if (i >= 0) a.splice(i, 1); };
window.getComputedStyle = function () { return { display: 'block', getPropertyValue: function () { return ''; } }; };
window.innerWidth = 1280; window.innerHeight = 800; window.devicePixelRatio = 1;
var location = { href: 'http://sweep/', pathname: '/', search: '', hash: '', origin: 'http://sweep' };
var navigator = { userAgent: 'sweep', clipboard: { writeText: function () { return Promise.resolve(); } } };
var performance = { now: function () { return 0; } };

// Timers record; nothing fires unless the sweep asks. A widget that schedules
// work is not wrong; a widget that schedules it and never clears it is, and
// the record is what lets the sweep say which.
var __seq = 0;
var setTimeout    = function (fn, ms) { __timers.timeouts.push({ id: ++__seq, fn: fn, ms: ms }); return __seq; };
var setInterval   = function (fn, ms) { __timers.intervals.push({ id: ++__seq, fn: fn, ms: ms }); return __seq; };
var clearTimeout  = function (id) { __timers.timeouts  = __timers.timeouts.filter(function (t) { return t.id !== id; }); };
var clearInterval = function (id) { __timers.intervals = __timers.intervals.filter(function (t) { return t.id !== id; }); };
var requestAnimationFrame = function (fn) { __timers.frames.push({ id: ++__seq, fn: fn }); return __seq; };
var cancelAnimationFrame  = function (id) { __timers.frames = __timers.frames.filter(function (t) { return t.id !== id; }); };

function ResizeObserver(cb) { this._cb = cb; }
ResizeObserver.prototype.observe = function () {};
ResizeObserver.prototype.unobserve = function () {};
ResizeObserver.prototype.disconnect = function () {};

function IntersectionObserver(cb) { this._cb = cb; }
IntersectionObserver.prototype.observe = function () {};
IntersectionObserver.prototype.disconnect = function () {};

// fetch → the desk's actions, in-process. __host(url) is a callable bound
// from Java; it returns the JSON body, or a string starting with "!" for a
// status.
globalThis.__fetches = [];
var fetch = function (url) {
    var r = __host(String(url));
    __fetches.push({ url: String(url), status: (typeof r === 'string' && r.charAt(0) === '!') ? (parseInt(r.slice(1), 10) || 500) : 200 });
    if (typeof r === 'string' && r.charAt(0) === '!') {
        var status = parseInt(r.slice(1), 10) || 500;
        return Promise.resolve({ ok: false, status: status, json: function () { return Promise.reject(new Error('no body')); }, text: function () { return Promise.resolve(''); } });
    }
    return Promise.resolve({ ok: true, status: 200, json: function () { return Promise.resolve(JSON.parse(r)); }, text: function () { return Promise.resolve(r); } });
};
var Response = function (body, init) { this._body = body; this.status = (init && init.status) || 200; this.ok = this.status >= 200 && this.status < 300; };
Response.prototype.json = function () { return Promise.resolve(JSON.parse(this._body)); };
Response.prototype.text = function () { return Promise.resolve(this._body); };
