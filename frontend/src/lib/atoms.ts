import { atom } from "jotai";
import type { FileHandle } from "./types";

export const fileAtom = atom<Map<string, FileHandle>>(new Map());