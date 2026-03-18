const API_BASE = 'http://localhost:8080/api';
function saveToken(t){ localStorage.setItem('token', t); }
function getToken(){ return localStorage.getItem('token'); }
function logout(){ localStorage.removeItem('token'); localStorage.removeItem('username'); localStorage.removeItem('balance'); localStorage.removeItem('role'); window.location='login.html'; }
async function api(path, method='GET', body){
  const headers = {'Content-Type':'application/json'}; const token = getToken(); if(token) headers['Authorization']='Bearer '+token;
  let res;
  try {
    res = await fetch(API_BASE+path,{method,headers,body: body?JSON.stringify(body):undefined});
  } catch(networkErr){ throw new Error('Network error'); }
  if(res.status===204) return null;
  const text = await res.text(); let json; try { json = text? JSON.parse(text): null; } catch(e){ json=null; }
  if(!res.ok){ const msg = json?.message || json?.error || 'Request failed'; const err = new Error(msg); err.status=res.status; err.payload=json; throw err; }
  return json;
}

// Login
const loginForm = document.getElementById('loginForm');
if(loginForm){
  loginForm.addEventListener('submit', async e => {
    e.preventDefault();
    const btn = loginForm.querySelector('button[type=submit]'); btn.disabled=true; btn.textContent='Signing in...';
    try{
      const usernameVal = loginUsername.value.trim(); const passVal = loginPassword.value;
      if(!usernameVal || !passVal){ throw new Error('Please fill all fields'); }
      const data = await api('/auth/login','POST',{username:usernameVal,password:passVal});
  saveToken(data.token); localStorage.setItem('username', data.username); localStorage.setItem('balance', data.balance); if(data.role) localStorage.setItem('role', data.role);
  NBUI.toast('Login successful. Redirecting...', {type:'success', timeout:1200});
  const dest = (data.role === 'ADMIN') ? 'admin.html' : 'dashboard.html';
  setTimeout(()=> window.location=dest, 600);
    }catch(err){ NBUI.toast(err.message || 'Login failed', {type:'error'}); }
    finally { btn.disabled=false; btn.textContent='Sign In'; }
  });
}

// Signup
const signupForm = document.getElementById('signupForm');
if(signupForm){
  signupForm.addEventListener('submit', async e => {
    e.preventDefault();
    const btn = signupForm.querySelector('button[type=submit]'); btn.disabled=true; btn.textContent='Creating...';
    try{
      const u = signupUsername.value.trim(); const em = signupEmail.value.trim(); const pw = signupPassword.value;
      if(pw.length<8) throw new Error('Password must be at least 8 characters');
      const data = await api('/auth/signup','POST',{username:u,email:em,password:pw});
  saveToken(data.token); localStorage.setItem('username', data.username); localStorage.setItem('balance', data.balance); if(data.role) localStorage.setItem('role', data.role);
  NBUI.toast('Account created. Redirecting...', {type:'success', timeout:1600});
  const dest = (data.role === 'ADMIN') ? 'admin.html' : 'dashboard.html';
  setTimeout(()=> window.location=dest, 700);
    }catch(err){ NBUI.toast(err.message || 'Signup failed', {type:'error'}); }
    finally { btn.disabled=false; btn.textContent='Create Account'; }
  });
}

// Forgot Password (OTP)
const forgotBtn = document.getElementById('forgotBtn');
if(forgotBtn){
  forgotBtn.addEventListener('click', async ()=>{
    const email = prompt('Enter your email to receive a reset OTP');
    if(!email) return;
    try { await api('/auth/forgot-password','POST',{email}); NBUI.toast('If the email exists, an OTP has been sent.', {type:'success'}); }
    catch(e){ NBUI.toast(e.message || 'Request failed', {type:'error'}); }
  });
}

// Generic logout button
const logoutBtn = document.getElementById('logoutBtn'); if(logoutBtn){ logoutBtn.addEventListener('click', ()=>{ logout(); }); }

window.BankAuth = { api, logout, getToken };

// Reusable password visibility toggles (login & signup)
['toggleLoginPw','toggleSignupPw'].forEach(id => {
  const btn = document.getElementById(id);
  if(!btn) return;
  const inputId = id === 'toggleLoginPw' ? 'loginPassword' : 'signupPassword';
  const field = document.getElementById(inputId);
  if(!field) return;
  btn.addEventListener('click', () => {
    const isPwd = field.type === 'password';
    field.type = isPwd ? 'text' : 'password';
    btn.textContent = isPwd ? 'Hide' : 'Show';
  });
});
