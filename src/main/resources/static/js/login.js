(function() {
  const allowedChars = ['0','1','2','3','4','5','6','7','8','9','#','*','^',')','(','@'];
  let password = '';
  let showPassword = false;

  const displayEl = document.getElementById('password-display');
  const toggleBtn = document.getElementById('toggle-password');
  const loginBtn = document.getElementById('login-btn');
  const emailInput = document.getElementById('login-email');

  function renderPassword() {
    displayEl.textContent = '';
    password.split('').forEach(function(char) {
      const span = document.createElement('span');
      span.textContent = showPassword ? char : '•';
      span.className = 'text-green-400 text-2xl font-mono';
      displayEl.appendChild(span);
    });
  }

  function updateLoginBtn() {
    if (emailInput.value.trim() && password.length >= 6 && password.length <= 16) {
      loginBtn.classList.remove('pointer-events-none', 'opacity-60');
    } else {
      loginBtn.classList.add('pointer-events-none', 'opacity-60');
    }
  }

  loginBtn.addEventListener('click', function() {
    if (loginBtn.classList.contains('opacity-60')) return;
    var email = emailInput.value.trim();
    if (!email || password.length < 6 || password.length > 16) return;

    loginBtn.disabled = true;
    fetch('/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email, password: password })
    })
      .then(function(res) { return res.json(); })
      .then(function(result) {
        if (result.success && result.data) {
            if (result.data.accessToken) {
                document.cookie = 'accessToken=' + result.data.accessToken + '; path=/; max-age=3600'; // 1시간
                localStorage.setItem('accessToken', result.data.accessToken);
            }
            if (result.data.refreshToken) {
                document.cookie = 'refreshToken=' + result.data.refreshToken + '; path=/; max-age=604800'; // 7일
                localStorage.setItem('refreshToken', result.data.refreshToken);
            }
            window.location.href = '/gacha/home';
        } else {
          alert(result.message || 'ログインに失敗しました');
          loginBtn.disabled = false;
        }
      })
      .catch(function() {
        alert('요청 실패');
        loginBtn.disabled = false;
      });
  });

  document.querySelectorAll('.keypad-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      var char = btn.textContent.trim();
      if (password.length < 16 && allowedChars.includes(char)) {
        password += char;
        renderPassword();
        updateLoginBtn();
      }
    });
  });

  document.getElementById('delete-password').addEventListener('click', function() {
    password = password.slice(0, -1);
    renderPassword();
    updateLoginBtn();
  });

  toggleBtn.addEventListener('click', function() {
    showPassword = !showPassword;
    toggleBtn.textContent = showPassword ? '非表示' : '表示';
    renderPassword();
  });

  emailInput.addEventListener('input', updateLoginBtn);
})();
