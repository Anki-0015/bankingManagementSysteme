import './auth.js';
const { toast, renderRecentTx, money } = window.NBUI || {};
const API_BASE = 'http://localhost:8080/api';
function getToken(){ return localStorage.getItem('token'); }
function logout(){ localStorage.removeItem('token'); localStorage.removeItem('username'); localStorage.removeItem('balance'); window.location='login.html'; }
async function api(path, method='GET', body){
  const headers = {'Content-Type':'application/json'}; const token = getToken(); if(token) headers['Authorization']='Bearer '+token;
  let res; try { res = await fetch(API_BASE+path,{method,headers,body: body?JSON.stringify(body):undefined}); } catch(e){ throw new Error('Network error'); }
  const text = await res.text(); let json; try { json = text? JSON.parse(text): null; } catch(e){ json = null; }
  if(!res.ok){ throw new Error(json?.message || 'Request failed'); }
  return json;
}
if(!getToken()) location='login.html';
const username = localStorage.getItem('username');
const welcome = document.getElementById('welcome'); if(welcome) welcome.textContent = username? ('Hi, '+ username): '';
const balanceDiv = document.getElementById('balance'); const lastUpdated = document.getElementById('lastUpdated');
function setBalance(b){ balanceDiv.textContent = money? money(b): b; localStorage.setItem('balance', b); if(lastUpdated) lastUpdated.textContent = 'Updated '+ new Date().toLocaleTimeString(); }
setBalance(localStorage.getItem('balance')||'0');

async function loadRecent(){
  try {
    const tx = await api('/account/transactions');
    if(tx && tx.length){
      // assume most recent first from backend; if not, sort by createdAt desc
      const sorted = tx.slice().sort((a,b)=> new Date(b.createdAt) - new Date(a.createdAt));
      setBalance(sorted[0].balance);
      if(renderRecentTx) renderRecentTx(sorted);
    }
  } catch(e){ /* silent */ }
}
loadRecent();

function handleSubmit(form, buildPayload, endpoint, successMsg){
  if(!form) return;
  form.addEventListener('submit', async e=>{
    e.preventDefault();
    const btn = form.querySelector('button[type=submit]'); if(btn){ btn.disabled=true; const prev=btn.textContent; btn.dataset.prev=prev; btn.textContent='Processing...'; }
    try {
      const payload = buildPayload();
      const newBal = await api(endpoint,'POST',payload);
      setBalance(newBal);
      toast && toast(successMsg, {type:'success', timeout:2500});
      form.reset();
      loadRecent();
    } catch(err){ toast && toast(err.message || 'Operation failed', {type:'error'}); }
    finally { const btn2 = form.querySelector('button[type=submit]'); if(btn2){ btn2.disabled=false; btn2.textContent=btn2.dataset.prev||'Submit'; } }
  });
}

handleSubmit(document.getElementById('depositForm'), ()=>({amount: depositAmount.value}), '/account/deposit', 'Deposit successful');
handleSubmit(document.getElementById('withdrawForm'), ()=>({amount: withdrawAmount.value}), '/account/withdraw', 'Withdrawal successful');
handleSubmit(document.getElementById('transferForm'), ()=>({targetUsername: transferTarget.value.trim(), amount: transferAmount.value}), '/account/transfer', 'Transfer successful');

const logoutBtn = document.getElementById('logoutBtn'); if(logoutBtn) logoutBtn.addEventListener('click', logout);
