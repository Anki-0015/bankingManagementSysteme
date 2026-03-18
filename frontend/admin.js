import './auth.js';
const { toast, money } = window.NBUI || {};
const API_BASE = 'http://localhost:8080/api';
function token(){ return localStorage.getItem('token'); }
if(!token()) location='login.html';

async function api(path, method='GET', body){
  const headers={'Content-Type':'application/json'}; const t=token(); if(t) headers.Authorization='Bearer '+t;
  let res; try { res = await fetch(API_BASE+path,{method,headers,body: body?JSON.stringify(body):undefined}); } catch(e){ throw new Error('Network error'); }
  const txt = await res.text(); let json; try { json = txt? JSON.parse(txt): null; } catch { json=null; }
  if(!res.ok) throw new Error(json?.message||json?.error||'Request failed');
  return json;
}

const tbody = document.querySelector('#usersTable tbody');
const empty = document.getElementById('usersEmpty');

function render(users){
  // remove skeleton rows
  tbody.querySelectorAll('.skeleton-row').forEach(r=>r.remove());
  if(!users.length){ tbody.innerHTML=''; empty.classList.remove('hidden'); return; }
  empty.classList.add('hidden');
  tbody.innerHTML = users.map(u=>{
    return `<tr data-id="${u.id}">
      <td>${u.id}</td>
      <td>${u.username}</td>
      <td>${u.email}</td>
      <td><input type="number" class="bal-input" value="${u.balance}" style="width:90px; background:var(--color-bg-alt); border:1px solid var(--color-border); color:var(--color-text); padding:.3rem .4rem; border-radius:4px; font-size:.65rem;" /></td>
      <td>
        <select class="role-select" style="background:var(--color-bg-alt); border:1px solid var(--color-border); color:var(--color-text); padding:.3rem .4rem; border-radius:4px; font-size:.65rem;">
          <option value="USER" ${u.role==='USER'?'selected':''}>USER</option>
          <option value="ADMIN" ${u.role==='ADMIN'?'selected':''}>ADMIN</option>
        </select>
      </td>
      <td class="text-right" style="display:flex; gap:.4rem;">
        <button class="btn outline gold save-btn" style="font-size:.55rem; padding:.4rem .55rem;">Save</button>
        <button class="btn outline danger del-btn" style="font-size:.55rem; padding:.4rem .55rem;">Del</button>
      </td>
    </tr>`;
  }).join('');
}

async function load(){
  try { const users = await api('/admin/users'); render(users); }
  catch(e){ toast && toast(e.message||'Failed to load users',{type:'error'}); }
}
load();

tbody.addEventListener('click', async e=>{
  const row = e.target.closest('tr[data-id]'); if(!row) return;
  const id = row.getAttribute('data-id');
  if(e.target.classList.contains('save-btn')){
    const balInput = row.querySelector('.bal-input');
    const roleSel = row.querySelector('.role-select');
    try {
      const newBal = balInput.value;
      await api(`/admin/users/${id}/balance`,'PATCH',{balance: newBal});
      await api(`/admin/users/${id}/role`,'PATCH',{role: roleSel.value});
      toast && toast('Updated',{type:'success',timeout:1600});
    } catch(err){ toast && toast(err.message||'Update failed',{type:'error'}); }
  } else if(e.target.classList.contains('del-btn')){
    if(!confirm('Delete user '+row.children[1].textContent+'?')) return;
    try { await api(`/admin/users/${id}`,'DELETE'); row.remove(); toast && toast('Deleted',{type:'success'}); }
    catch(err){ toast && toast(err.message||'Delete failed',{type:'error'}); }
    if(!tbody.children.length) empty.classList.remove('hidden');
  }
});

// Create user
const form = document.getElementById('createUserForm');
if(form){
  form.addEventListener('submit', async e=>{
    e.preventDefault();
    const btn = form.querySelector('button[type=submit]'); const prev=btn.textContent; btn.disabled=true; btn.textContent='Creating...';
    try {
      const payload = { username:newUsername.value.trim(), email:newEmail.value.trim(), password:newPassword.value, role:newRole.value };
      if(!payload.username || !payload.email || payload.password.length<4) throw new Error('Fill all fields');
      await api('/admin/users','POST',payload);
      toast && toast('User created',{type:'success'});
      form.reset();
      load();
    } catch(err){ toast && toast(err.message||'Create failed',{type:'error'}); }
    finally { btn.disabled=false; btn.textContent=prev; }
  });
}

// Restrict access if non-admin (best-effort client side; server still enforces)
(async ()=>{
  try {
    const users = await api('/admin/users');
    // if call succeeded but list excludes current user with ADMIN role, it's still okay; we don't expose more here.
  } catch(err){
    toast && toast('Access denied (admin only)',{type:'error'});
    setTimeout(()=>location='dashboard.html',1500);
  }
})();

const logoutBtn = document.getElementById('logoutBtn'); if(logoutBtn) logoutBtn.addEventListener('click', ()=>{ localStorage.clear(); location='login.html'; });
