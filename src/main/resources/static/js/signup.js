(function() {
  const allowedChars = ['0','1','2','3','4','5','6','7','8','9','#','*','^',')','(','@'];
  let password = '';
  let confirmPassword = '';
  let showPassword = false;
  let activeInput = 'password';

  const passwordDisplay = document.getElementById('password-display');
  const confirmDisplay = document.getElementById('confirm-display');
  const passwordValue = document.getElementById('signup-password-value');
  const confirmValue = document.getElementById('signup-confirm-value');
  const toggleBtn = document.getElementById('toggle-password');
  const submitBtn = document.getElementById('submit-btn');
  const activePasswordBtn = document.getElementById('active-password');
  const activeConfirmBtn = document.getElementById('active-confirm');
  const passwordCount = document.getElementById('password-count');
  const confirmStatus = document.getElementById('confirm-status');

  function render(str, el) {
    el.textContent = '';
    str.split('').forEach(function(char) {
      const span = document.createElement('span');
      span.textContent = showPassword ? char : '•';
      span.className = 'text-green-400 text-lg font-mono';
      el.appendChild(span);
    });
  }

  function updateSubmit() {
    const match = password === confirmPassword && password.length >= 6;
    const hasEmail = document.getElementById('signup-email').value;
    const hasNickname = document.getElementById('signup-nickname').value;
    submitBtn.disabled = !(hasEmail && hasNickname && password.length >= 6 && match);
  }

  function updateConfirmStatus() {
    if (!confirmPassword) {
      confirmStatus.innerHTML = '';
      return;
    }
    if (password === confirmPassword) {
      confirmStatus.innerHTML = '<span class="text-green-400">✓</span>';
    } else {
      confirmStatus.innerHTML = '<span class="text-red-400">✕</span>';
    }
  }

  document.querySelectorAll('.keypad-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      var ch = btn.textContent;
      if (!allowedChars.includes(ch)) return;
      if (activeInput === 'password' && password.length < 16) {
        password += ch;
        render(password, passwordDisplay);
        passwordCount.textContent = password.length + '/16';
      } else if (activeInput === 'confirm' && confirmPassword.length < 16) {
        confirmPassword += ch;
        render(confirmPassword, confirmDisplay);
      }
      updateConfirmStatus();
      updateSubmit();
    });
  });

  document.getElementById('delete-password').addEventListener('click', function() {
    if (activeInput === 'password') {
      password = password.slice(0, -1);
      render(password, passwordDisplay);
      passwordCount.textContent = password.length + '/16';
    } else {
      confirmPassword = confirmPassword.slice(0, -1);
      render(confirmPassword, confirmDisplay);
    }
    updateConfirmStatus();
    updateSubmit();
  });

  toggleBtn.addEventListener('click', function() {
    showPassword = !showPassword;
    toggleBtn.textContent = showPassword ? '숨기기' : '보기';
    render(password, passwordDisplay);
    render(confirmPassword, confirmDisplay);
  });

  activePasswordBtn.addEventListener('click', function() {
    activeInput = 'password';
    activePasswordBtn.className = 'py-2 px-3 rounded-lg text-sm transition-colors bg-green-700 text-white';
    activeConfirmBtn.className = 'py-2 px-3 rounded-lg text-sm transition-colors bg-gray-700 text-gray-300';
  });

  activeConfirmBtn.addEventListener('click', function() {
    activeInput = 'confirm';
    activeConfirmBtn.className = 'py-2 px-3 rounded-lg text-sm transition-colors bg-green-700 text-white';
    activePasswordBtn.className = 'py-2 px-3 rounded-lg text-sm transition-colors bg-gray-700 text-gray-300';
  });

  document.getElementById('signup-email').addEventListener('input', updateSubmit);
  document.getElementById('signup-nickname').addEventListener('input', updateSubmit);

  document.querySelector('form').addEventListener('submit', function(e) {
    passwordValue.value = password;
    confirmValue.value = confirmPassword;
  });
})();
