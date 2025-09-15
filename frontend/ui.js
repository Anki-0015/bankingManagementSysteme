// UI Utilities: toasts, theme, formatting, helpers
(function(){
  const storeKey = 'nb_theme';
  const prefersLight = window.matchMedia('(prefers-color-scheme: light)');
  function applyTheme(theme){
    if(theme==='light') document.body.classList.add('light'); else document.body.classList.remove('light');
    localStorage.setItem(storeKey, theme);
    const toggle = document.getElementById('themeToggle'); if(toggle) toggle.checked = theme==='light';
  }
  function initTheme(){
    const saved = localStorage.getItem(storeKey);
    if(saved){ applyTheme(saved); } else if(prefersLight.matches){ applyTheme('light'); }
    const toggle = document.getElementById('themeToggle');
    if(toggle){ toggle.addEventListener('change', e=> applyTheme(e.target.checked?'light':'dark')); }
  }
  // Toasts
  function toast(msg, {type='info', timeout=4000}={}){
    const wrap = document.getElementById('toastContainer'); if(!wrap) return console.warn('Toast container missing');
    const el = document.createElement('div'); el.className = 'toast '+(type==='error'?'error': type==='success'?'success':'');
    el.innerHTML = `<span style="flex:1;">${escapeHtml(msg)}</span><button class="close" aria-label="Close">×</button>`;
    wrap.appendChild(el);
    const close = ()=>{ if(el.dataset.closing) return; el.dataset.closing='1'; el.style.opacity='0'; el.style.transform='translateX(30px)'; setTimeout(()=>el.remove(), 320); };
    el.querySelector('.close').addEventListener('click', close);
    if(timeout>0) setTimeout(close, timeout);
    return close;
  }
  function escapeHtml(str){ return str.replace(/[&<>"']/g, c=>({"&":"&amp;","<":"&lt;",">":"&gt;","\"":"&quot;","'":"&#39;"}[c])); }
  // Formatting
  function money(val){ if(val==null||val==='') return '--'; const num = Number(val); if(isNaN(num)) return val; return Intl.NumberFormat(undefined,{style:'currency',currency:'USD'}).format(num); }
  function isoToLocal(iso){ if(!iso) return ''; try { return new Date(iso).toLocaleString(); } catch(e){ return iso; } }
  // Export CSV
  function exportTableToCSV(table, filename='transactions.csv'){
    if(!table) return; const rows = [...table.querySelectorAll('tr')];
    const csv = rows.map(r=>[...r.children].map(c=>`"${c.textContent.replace(/"/g,'""')}"`).join(',')).join('\n');
    const blob = new Blob([csv], {type:'text/csv;charset=utf-8;'}); const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href=url; a.download=filename; document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url);
  }
  // Recent transactions renderer
  function renderRecentTx(list){
    const container = document.getElementById('recentList'); const empty = document.getElementById('recentEmpty'); if(!container) return;
    if(!list || list.length===0){ if(empty) empty.classList.remove('hidden'); container.innerHTML=''; return; }
    if(empty) empty.classList.add('hidden');
    container.innerHTML = list.slice(0,6).map(t=>{
      const sign = t.type.includes('DEPOSIT')||t.type.endsWith('_IN');
      const amtClass = sign? 'amount-pos' : 'amount-neg';
      return `<div class="flex gap-sm" style="justify-content:space-between; align-items:center; padding:.55rem .65rem; background:var(--color-surface-alt); border:1px solid var(--color-border); border-radius: var(--radius-md);">
        <div style="display:flex; flex-direction:column; gap:.25rem;">
          <span class="badge ${t.type.toLowerCase()}">${t.type.replace('_',' ')}</span>
          <span style="font-size:.6rem; color:var(--color-text-soft);">${isoToLocal(t.createdAt)}</span>
        </div>
        <div class="${amtClass}" style="font-size:.9rem; font-weight:600;">${sign?'+':'-'}${money(t.amount)}</div>
      </div>`; }).join('');
  }
  window.NBUI = { toast, money, isoToLocal, exportTableToCSV, renderRecentTx };
  initTheme();
})();
