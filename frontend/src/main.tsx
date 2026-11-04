import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App'
import { OturumSaglayici } from './OturumSaglayici'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <OturumSaglayici>
      <App />
    </OturumSaglayici>
  </StrictMode>,
)