const { toast, money, isoToLocal, exportTableToCSV } = window.NBUI || {};
const API_BASE = 'http://localhost:8080/api';
function getToken(){ return localStorage.getItem('token'); }
function logout(){ localStorage.removeItem('token'); localStorage.removeItem('username'); localStorage.removeItem('balance'); window.location='login.html'; }
async function api(path, method='GET', body){
  const headers = {'Content-Type':'application/json'}; const token=getToken(); if(token) headers['Authorization']='Bearer '+token;
  let res; try { res = await fetch(API_BASE+path,{method,headers,body: body?JSON.stringify(body):undefined}); } catch(e){ throw new Error('Network error'); }
  const text = await res.text(); let json; try { json = text? JSON.parse(text): null; } catch(e){ json=null; }
  if(!res.ok){ throw new Error(json?.message || 'Request failed'); }
  return json;
}
if(!getToken()) location='login.html';
const tbody = document.querySelector('#txTable tbody');
const tableEmpty = document.getElementById('tableEmpty');
const searchInput = document.getElementById('searchInput');
const typeFilter = document.getElementById('typeFilter');
const exportBtn = document.getElementById('exportBtn');
let allTx = [];

function render(list){
  if(!list.length){ tbody.innerHTML=''; tableEmpty?.classList.remove('hidden'); return; }
  tableEmpty?.classList.add('hidden');
  tbody.innerHTML = list.map(t=>{
    const signPos = t.type==='DEPOSIT' || t.type==='TRANSFER_IN';
    const amtClass = signPos? 'amount-pos':'amount-neg';
    const badgeClass = 'badge '+t.type.toLowerCase();
    return `<tr>
      <td>${t.id}</td>
      <td><span class="${badgeClass}">${t.type.replace('_',' ')}</span></td>
      <td class="${amtClass}">${signPos?'+':'-'}${money?money(t.amount):t.amount}</td>
      <td>${t.description? t.description.replace(/</g,'&lt;') : ''}</td>
      <td>${isoToLocal? isoToLocal(t.createdAt): t.createdAt}</td>
    </tr>`; }).join('');
}

function applyFilters(){
  const q = (searchInput?.value || '').toLowerCase().trim();
  const typeVal = typeFilter?.value || '';
  let list = allTx.slice();
  if(typeVal) list = list.filter(t=> t.type===typeVal);
  if(q) list = list.filter(t=> (t.description||'').toLowerCase().includes(q) || t.type.toLowerCase().includes(q));
  render(list);
}

async function load(){
  try {
    const list = await api('/account/transactions');
    // sort by createdAt desc
    allTx = list.slice().sort((a,b)=> new Date(b.createdAt)-new Date(a.createdAt));
    applyFilters();
    toast && toast('Transactions loaded', {type:'success', timeout:1800});
  } catch(e){ tbody.innerHTML=''; tableEmpty?.classList.remove('hidden'); tableEmpty.textContent='Failed to load'; toast && toast(e.message || 'Failed to load', {type:'error'}); }
}
load();

searchInput?.addEventListener('input', ()=> applyFilters());
typeFilter?.addEventListener('change', ()=> applyFilters());
exportBtn?.addEventListener('click', ()=> exportTableToCSV && exportTableToCSV(document.getElementById('txTable')));
const logoutBtn = document.getElementById('logoutBtn'); if(logoutBtn) logoutBtn.addEventListener('click', logout);
