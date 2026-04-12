"use client";

import { useCallback, useEffect, useState } from "react";

/**
 * Imágenes en la raíz de `public/` con el mismo nombre que en BD + `.jpg`
 * Ej.: `/Pensatorium.jpg`, `/Santo%20Grial.jpg`, `/La%20Dama%20del%20Mar.jpg`
 */
export function urlImagenCartaAccion(nombre: string): string {
  return `/${encodeURIComponent(nombre.trim())}.jpg`;
}

export type CartaAccionFichaProps = {
  nombre: string;
  descripcion: string;
  onClick?: () => void;
  disabled?: boolean;
  /** Resaltar cuando hay modo de acción activo */
  modoAccionActivo?: boolean;
  /** Modal de elección: cartas más anchas; mano: ancho completo */
  variante: "elegir" | "mano";
  className?: string;
};

export function CartaAccionFicha({
  nombre,
  descripcion,
  onClick,
  disabled,
  modoAccionActivo,
  variante,
  className = "",
}: CartaAccionFichaProps) {
  const [imgOk, setImgOk] = useState(true);
  const src = urlImagenCartaAccion(nombre);

  useEffect(() => {
    setImgOk(true);
  }, [nombre, src]);

  const onImgError = useCallback(() => {
    setImgOk(false);
  }, []);

  const esElegir = variante === "elegir";

  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onClick}
      className={[
        "group relative overflow-hidden rounded-xl border-2 border-white text-left shadow-lg transition-all",
        esElegir ? "w-44 shrink-0 hover:scale-[1.02] active:scale-[0.99]" : "w-full",
        modoAccionActivo ? "ring-2 ring-yellow-400 ring-offset-2 ring-offset-[#111d2c]" : "",
        disabled ? "opacity-40 cursor-not-allowed" : "cursor-pointer hover:brightness-[1.05]",
        esElegir ? "min-h-[200px]" : "min-h-[118px]",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {imgOk ? (
        // eslint-disable-next-line @next/next/no-img-element -- src dinámico por nombre de carta
        <img
          src={src}
          alt=""
          className="absolute inset-0 h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
          onError={onImgError}
        />
      ) : (
        <div className="absolute inset-0 bg-gradient-to-br from-[#4a3420] to-[#2a1d12]" aria-hidden />
      )}
      <div
        className="absolute inset-0 bg-gradient-to-t from-black/92 via-black/55 to-black/30"
        aria-hidden
      />
      <div
        className={`relative z-10 flex h-full flex-col justify-end ${
          esElegir ? "gap-2 p-4" : "gap-1 p-2.5"
        }`}
      >
        <span
          className={`font-bold uppercase tracking-wider text-white drop-shadow-[0_1px_2px_rgba(0,0,0,0.95)] ${
            esElegir ? "text-xs leading-tight" : "text-[10px] leading-tight"
          }`}
        >
          {nombre}
        </span>
        <span
          className={`leading-snug text-white/95 drop-shadow-md ${
            esElegir ? "text-[11px]" : "text-[8px] leading-tight"
          }`}
        >
          {descripcion}
        </span>
      </div>
    </button>
  );
}
