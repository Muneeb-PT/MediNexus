const sections=[...document.querySelectorAll('.page-section')];
const links=[...document.querySelectorAll('nav a')];
function navigate(){
  const id=location.hash.replace('#','')||'dashboard';
  sections.forEach(s=>s.classList.toggle('hidden',s.id!==id));
  links.forEach(a=>a.classList.toggle('active',a.getAttribute('href')==='#'+id));
  const titles={dashboard:'Hospital Overview',patients:'Patient Management',appointments:'Appointments',doctors:'Doctor Management',prescriptions:'Electronic Prescriptions',billing:'Billing & Invoices',inventory:'Medicine Inventory'};
  document.getElementById('pageTitle').textContent=titles[id]||'Hospital Overview';
}
window.addEventListener('hashchange',navigate); navigate();
function openModal(id){document.getElementById(id).classList.add('show')}
function closeModal(id){document.getElementById(id).classList.remove('show')}
function showToast(msg){const t=document.getElementById('toast');t.textContent=msg;t.classList.add('show');clearTimeout(window.toastTimer);window.toastTimer=setTimeout(()=>t.classList.remove('show'),2200)}
function filterRows(input,id){const q=input.value.toLowerCase();document.querySelectorAll('#'+id+' tbody tr').forEach(r=>r.style.display=r.innerText.toLowerCase().includes(q)?'':'none')}
document.querySelectorAll('.modal-backdrop').forEach(m=>m.addEventListener('click',e=>{if(e.target===m)m.classList.remove('show')}));
